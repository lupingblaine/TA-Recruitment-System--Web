> 已支持：[回复截止时间、倒计时与逾期处理](REPLY_DEADLINES_zh.md)。

# 发布 TA 录用安排与学生确认

## 负责人发布

1. Organiser / 管理员打开 **TA Assignments**，选定申请周期与课程，从 Offer 申请中保存分配草稿。
2. 草稿会预留名额并参与排课冲突检查，但学生看不到草稿，也不会收到发布通知。
3. 检查 Section 的上课时间、地点和工时后，点击该记录上的 **Publish result**，设置回复截止时间，核对后确认发布。
4. 发布后状态为 **Awaiting student reply**，学生收到站内通知。重复发布会被阻止。
5. 用 **Result status** 筛选 Draft、Awaiting reply、Accepted、Declined 或 Cancelled；课程容量同时显示 reserved 和 accepted 人数。

## 学生回复

1. 从通知中的 **Review assignment** 或顶部通知链接进入 **My Assignments**；也可直接点击侧栏入口。
2. 核对每周上课时段、校园时区、地点及每周工时。
3. 点击 **Accept assignment** 并确认，或者点击 **Decline assignment**，填写拒绝原因并确认。
4. 每条发布记录只接受一次回复。回复保存时间，负责人收到站内通知；拒绝会释放该 Section 在该周期的预留名额，并保留原因与原始分配快照。

按每条 Section 分配独立回复，不会将同一申请下的其他 Section 一并接受或拒绝。拒绝某条分配不会将整个申请改成 Rejected，负责人仍可另行安排兼容的 Section。

## 名额、工时与取消

- **Reserved**：所有尚未取消的分配，包括草稿、待回复、已接受；用来防止超额、重复分配和同一学生排课冲突。
- **Accepted**：学生已接受且尚未取消的分配，代表已确认到岗人数和工时。待回复不计入已确认。
- 学生只能看到已发布安排，其“published hours”也不包含隐藏草稿；“accepted hours”仅统计已接受且有效的安排。
- 发布时必须设置回复截止时间，逾期自动释放预留名额；详情见 [回复截止时间](REPLY_DEADLINES_zh.md)。
- 负责人仍可填写原因取消草稿、待回复或已接受的安排。已发布的取消会通知学生；未发布草稿取消不通知。学生原回复及时间不会被覆盖。
- 学生接受后不能直接改成拒绝；如需变更，联系负责人取消后重新安排。重新安排产生新草稿，需重新发布和回复。
- 被拒绝、已取消的记录不能重新发布或回复。新负责人接管课程后负责接收回复通知。
- 不发送邮件、不接入收费服务。通知写入项目自己的 PostgreSQL，并随发布/回复在同一事务提交。

## 升级与接口

V6 为 `section_assignments` 增加发布状态、发布人/时间、回复状态、回复时间及原因。现有 V5 分配全部保留为 **Draft / Pending**，不自动发布、不代替学生接受、不补发通知；已取消的仍然取消。负责人确认后再发布有效草稿。

- `POST /api/organiser/assignments/{id}/publish`：发布自己的课程分配；管理员用 `/api/admin/assignments/{id}/publish`。
- `POST /api/applicant/assignments/{id}/respond`：`{"decision":"accept"}` 或 `{"decision":"decline","reason":"..."}`。拒绝原因必须为非空文本，最多 1000 字符。
- 原有列表与取消接口保持不变。学生列表不返回草稿、人工确认说明或工作人员身份字段。
- 通知类型为 `assignment_published`、`assignment_response`、`assignment_cancelled`；关联 ID 指向对应分配。发布、回复和取消均有审计记录。
- 数据库事务与跨进程锁保证重复/同时回复只有一次生效；拒绝与名额释放同时提交。

申请列表中的 Offer 仍是申请审核阶段，正式 Section 安排以这里的发布结果为准。
