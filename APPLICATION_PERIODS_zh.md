# 申请周期管理

本轮保留逐职位投递方式，增加全局申请周期。每次新提交都记录所属周期。

## 管理员操作

1. 使用管理员账号登录，打开侧栏 **Application Periods**。
2. 点击 **Create period**，填写名称、所属学期、开放时间和截止时间。
3. 保存后为 `draft`，学生尚不能提交。
4. 点击 **Open intake** 启用周期。若开放时间尚未到，显示 `scheduled`；到时自动允许提交。
5. 点击 **Close intake** 可提前关闭。工作人员仍能查看、审核现有申请。
6. 点击 **Reopen** 可恢复接收；若截止时间已过，先通过 **Edit** 延长截止时间。

学期来自 **Properties → Global Terms**。没有合适学期时，先添加学期。
日期选择器固定显示英文月份、星期和 24 小时时间，不随浏览器语言切换。控件随项目本地提供，无需额外 CDN 请求。
日期按操作人员浏览器的本地时区填写和显示，后端统一存储为带时区的 UTC 时间。
开放时间包含在可提交范围内，截止时刻起不再接受提交，无需定时任务或重启。

同时最多允许一个周期处于启用状态（包括尚未开始的 scheduled 周期）。
要启用下个周期，先关闭上个周期；已过截止时间的启用周期也需手动关闭。
本轮周期限制适用于所有 active 职位，不按课程或院系分别开放。

## 学生与 Organiser

学生的 Dashboard、Find Jobs、职位详情和 Application Tracker 页面显示当前周期状态。
截止后可以继续查看历史申请，后端会拒绝新的提交；伪造请求或旧页面不能绕过检查。
已打开的申请弹窗还会校验周期 ID，防止误投到另一个刚启用的周期。

提交前还需保存本周期的 [每周可用时间](WEEKLY_AVAILABILITY_zh.md)，每份申请保留独立快照。
同一学生在同一周期只能投递同一职位一次；下一周期可再次申请。
Application Tracker 可按周期筛选，Organiser 的 Pipeline 也新增周期筛选。
筛选不会扩大 Organiser 的课程权限。

管理员通过每个周期的 **View applications** 查看该周期的申请人、职位和审核状态。
修改周期名称和时间不会修改申请状态；有申请后不能更换所属学期。

公开注册仅能创建 Applicant 账号。Organiser 和 Admin 由管理员在 Users 页面创建，
已有工作人员账号仍可正常登录。

## 历史数据与升级

启动时自动执行 V2 数据库迁移。已有申请保留原 ID 和审核状态，
归入只读、关闭的 **Historical applications** 周期。
历史周期不能重新开放，且不会自动创建或开放正式申请周期。
因此升级完成后，需要管理员创建并开放周期，学生才能提交新的申请。

迁移不会重新读取 JSON 覆盖已存在的 PostgreSQL 数据。
本次升级前的数据库备份位于 `backups/tamo-before-application-periods.dump`。
常规备份方法见 [数据库说明](DATABASE_zh.md)。

## API

| 方法 | 路径（前缀 `/tamo/api`） | 权限与用途 |
|---|---|---|
| GET | `/admin/application-periods` | 管理员查看所有周期 |
| POST | `/admin/application-periods` | 创建草稿 |
| PUT | `/admin/application-periods/{id}` | 修改名称、学期和时间 |
| POST | `/admin/application-periods/{id}/open` | 开放或重新开放 |
| POST | `/admin/application-periods/{id}/close` | 关闭 |
| GET | `/admin/application-periods/{id}/applications` | 管理员查看该周期申请 |
| GET | `/organiser/application-periods` | Organiser 查看周期列表 |
| GET | `/organiser/pipeline?periodId={id}` | 按周期筛选有权审核的申请 |
| GET | `/applicant/application-period` | 学生查看当前接收状态 |
| GET | `/applicant/applications?periodId={id}` | 查看本人指定周期申请 |

创建和编辑请求字段为 `name`、`termId`、`opensAt`、`closesAt`。
时间示例：`2026-10-16T12:00:00-07:00` 或 `2026-10-16T19:00:00Z`。
客户端不能直接设置周期状态，必须调用 open / close 接口。
提交职位申请可附带 `periodId`，服务端检查其与当前开放周期是否一致。

窗口关闭、尚未开放或已经截止时，新提交返回 403。
同周期重复申请、旧弹窗周期不一致、并行启用多个周期等冲突返回 409。
日期、学期等字段无效时返回 400。所有重要周期变更记录操作人员和时间。
