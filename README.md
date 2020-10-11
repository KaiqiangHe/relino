# relino

reliable-notice 

## 设计

对于一个操作，要么成功，要么失败（操作失败、超时失败）

对于一个操作，失败后是否可以重试



```
notice(op1, time, max).finish(
success -> (op2, )
failed -> (op3, )
)

main-ops (成功或失败，支持将某些内容更新回记录中)
main-succ-ops
main-failed-ops

CommonOps
ReliableOps



1. 操作
2. 通知开始时间-支持延迟
3. 请求次数
3.1 最多一次
3.2 最少一次


```

### 消息传递次数

* 最多一次
* 最少一次