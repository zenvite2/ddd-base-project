# Temporal Cross-Service Communication

> Tài liệu ghi nhận các pattern giao tiếp giữa các service qua Temporal trong NTBH Platform.

## Nguyên tắc cốt lõi

Temporal dispatch activity dựa trên **Task Queue**, không phải interface:

```
Workflow tạo ActivityStub(Interface, taskQueue="X")
  → Temporal Server đẩy task vào queue "X"
    → Worker nào poll queue "X" thì nhận và execute
```

**Quy tắc vàng:**

```
1 ActivityInterface + 1 Task Queue = 1 Owner (1 service chịu trách nhiệm implement)
```

- **Interface** = contract (service nào cũng có thể depend để tạo stub)
- **Task Queue** = routing (quyết định service nào xử lý)
- Service muốn gọi → tạo stub trỏ đúng queue, **không implement lại**

---

## 3 Pattern giao tiếp cross-service

### Pattern 1: Activity gọi HTTP/gRPC

Activity trong Service A gọi REST API của Service B. Service B không cần biết Temporal.

```java
// Activity impl trong Purchase Service — gọi HTTP sang Payment Service
@Component
public class PaymentActivitiesImpl implements PaymentActivities {
    private final PaymentClient paymentClient;

    @Override
    public PaymentResult requestPayment(PaymentRequest request) {
        return paymentClient.createPayment(request);
    }
}
```

| Ưu điểm | Nhược điểm |
|---|---|
| Đơn giản, Service B không cần Temporal | Temporal chỉ retry HTTP call, không orchestrate bên trong Service B |
| Không cần shared artifact | Cần quản lý HTTP client, circuit breaker riêng |

### Pattern 2: Shared Activity via Task Queue (Temporal-native)

Service A schedule activity lên task queue mà Service B poll. Không cần HTTP.

```java
// Shared contract (module riêng, ví dụ ntbh-contracts)
@ActivityInterface
public interface PolicyActivities {
    @ActivityMethod
    PolicyResult issuePolicy(PolicyRequest request);
}

// Service B (Policy Service): implement + đăng ký worker
@Component
public class PolicyActivitiesImpl implements PolicyActivities {
    @Override
    public PolicyResult issuePolicy(PolicyRequest request) {
        return policyFacade.issuePolicy(request);
    }
}

@Component
public class PolicyWorkerRegistrar implements WorkerRegistrar {
    @Override public String taskQueue() { return "policy-act"; }
    @Override public Class<?>[] workflowImplementationTypes() { return new Class[0]; }
    @Override public Object[] activityImplementations() { return new Object[]{policyActivities}; }
}

// Service A (Purchase Service): workflow tạo stub trỏ sang queue Service B
private final PolicyActivities policyActivities = Workflow.newActivityStub(
    PolicyActivities.class,
    ActivityOptions.newBuilder()
        .setTaskQueue("policy-act")
        .build());
```

| Ưu điểm | Nhược điểm |
|---|---|
| Không cần HTTP, Temporal quản lý retry/timeout | Cần share activity interface giữa các service |
| Type-safe, native routing | Service B phải có Temporal worker |

### Pattern 3: Child Workflow

Service A start child workflow chạy trên task queue của Service B. Dùng khi Service B cần orchestrate nhiều bước phức tạp.

```java
// Shared contract
@WorkflowInterface
public interface PolicyIssuanceWorkflow {
    @WorkflowMethod
    PolicyResult execute(PolicyIssuanceInput input);
}

// Trong workflow của Service A
PolicyIssuanceWorkflow child = Workflow.newChildWorkflowStub(
    PolicyIssuanceWorkflow.class,
    ChildWorkflowOptions.newBuilder()
        .setTaskQueue("policy-wf")
        .setWorkflowId("policy-issuance-" + input.getOrderId())
        .build());

PolicyResult result = child.execute(policyInput);
```

| Ưu điểm | Nhược điểm |
|---|---|
| Service B tự orchestrate nội bộ | Phức tạp nhất |
| Parent theo dõi/cancel/signal child được | Cần share workflow interface |

---

## Bảng so sánh

| Tiêu chí | Pattern 1 (HTTP) | Pattern 2 (Shared Activity) | Pattern 3 (Child Workflow) |
|---|---|---|---|
| Service B cần Temporal? | Không | Có | Có |
| Shared artifact? | Không | Activity interface | Workflow interface |
| Retry/timeout | Temporal retry HTTP call | Temporal native | Temporal native |
| Độ phức tạp | Thấp | Trung bình | Cao |
| Khi nào dùng | Service B đơn giản / hệ thống ngoài | Gọi 1 thao tác đơn lẻ bên Service B | Service B cần orchestrate nhiều bước |

---

## Quy tắc routing — Tránh conflict

| Tình huống | Kết quả | Đúng/Sai |
|---|---|---|
| 2 instance cùng service poll cùng queue | Load balancing (scale horizontal) | Đúng |
| 2 service khác nhau impl khác nhau, poll cùng queue | Kết quả không deterministic | **SAI — anti-pattern** |
| 2 service depend cùng interface, chỉ 1 service implement + poll | Routing chính xác qua task queue | Đúng |

---

## Kiến trúc đề xuất cho NTBH Platform

### Shared contracts module

Đặt tất cả activity/workflow interface dùng chung trong 1 module riêng (ví dụ `ntbh-temporal-contracts` hoặc trong `ntbh-common`).

```
ntbh-temporal-contracts/
├── payment/
│   └── PaymentActivities.java
├── policy/
│   └── PolicyActivities.java
└── notification/
    └── NotificationActivities.java
```

### Sơ đồ tổng quan

```
                        ┌─────────────────────┐
                        │  ntbh-contracts      │
                        │  (shared interfaces) │
                        │                      │
                        │  PaymentActivities   │
                        │  PolicyActivities    │
                        │  NotifActivities     │
                        └──────────┬───────────┘
                                   │ depend
                 ┌─────────────────┼─────────────────┐
                 │                 │                  │
        Purchase Service    Payment Service     Policy Service
        ─────────────────   ────────────────    ───────────────
        Workflow poll:      Activity poll:      Activity poll:
        "purchase-wf"       "payment-act"       "policy-act"
                            impl Payment-       impl Policy-
                            Activities          Activities
```

### Ví dụ end-to-end: Purchase flow

```java
public class PurchaseWorkflowImpl implements PurchaseWorkflow {

    private final PaymentActivities payment = Workflow.newActivityStub(
        PaymentActivities.class,
        ActivityOptions.newBuilder().setTaskQueue("payment-act").build());

    private final PolicyActivities policy = Workflow.newActivityStub(
        PolicyActivities.class,
        ActivityOptions.newBuilder().setTaskQueue("policy-act").build());

    @Override
    public PurchaseResult execute(PurchaseInput input) {
        PaymentResult paid = payment.charge(input.getPaymentRequest());
        PolicyResult issued = policy.issuePolicy(input.getPolicyRequest());
        return new PurchaseResult(paid, issued);
    }
}
```

---

## Khuyến nghị triển khai

1. **Bắt đầu với Pattern 1** (Activity + HTTP) — đơn giản, service nhận không cần Temporal
2. **Chuyển sang Pattern 2** khi service đích đã tích hợp Temporal và cần reliability cao hơn HTTP
3. **Dùng Pattern 3** chỉ khi service đích có workflow phức tạp nhiều bước
4. **Shared contract** nên là module Maven riêng, publish lên Nexus để các service depend
