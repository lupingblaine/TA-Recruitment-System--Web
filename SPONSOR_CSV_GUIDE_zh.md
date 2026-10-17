# Sponsor 表单与 CSV 使用说明

当前版本按 `Test_Data` 三份 CSV 和 sponsor 邮件调整。本说明优先于旧文档中“CSV 延后”的说明。Version 1 当前数据库迁移版本为 14；原始 CSV 不修改。

## 学生填写

在 **TA Application Profile** 填写 First Name、Last Name、UCI Email、8 位学号、Student Level、Academic Program、研究生入学年份、Faculty/PI、非暑期 UCI TA 季度数、candidacy、TA Training、Online Sexual Harassment Training、额外校内工作、DSC accommodation 及安排说明。研究生须填入学年份；本科生可留空。没有额外工作填 None，没有 PI 可填 N/A。

Skills 改为老师提供的复选选项，默认词汇参考样本（Genetics、Lab Tech、Python、R、Linux、Git、Slurm 等）。Courses of Interest 从系统课程多选。可另填教学与科研经历。两项 sponsor 声明分别确认：已查阅课程表减少排班冲突；按时注册至少 12 units、支付 Zot 余额后才能应用 tuition remission。另外保留信息真实性确认。

正常在线申请仍需保存当周期 Weekly Availability。自由文字 Scheduling Conflicts 是额外说明；结构化每周时间是在线匹配依据。每份提交保存独立快照；后改名字、培训或老师技能选项不会改写旧申请。

## 老师设置

Version 1 创建职位时选择 Course 和同一周期的 Section，自动显示每周上课时间；旧工作日期生成器已移除。多个 Section 表示职位覆盖范围，实际 TA 安排仍需分别分配。完整版本说明见 [Version 1 版本说明](VERSION_1_zh.md)。

在 **Course Sections** 选择课程和周期，添加 Lecture、Discussion 或 Lab 的名称、每周时间、名额、Faculty 及周工时。启用 **Use Section-specific requirements** 后可分别设置 Required Skills、Preferred Skills、Additional skill options、TA Training 与 Online Sexual Harassment Training 要求；未启用时使用职位条件。

**Matching Criteria** 仍可设置职位层面的技能、层级及培训条件，也能增加提供给学生的技能选项。必需/优先技能会自动加入学生词汇。老师只能配置自己拥有的课程，管理员可配置所有课程。

审核页面显示每个 Section 的资格缺项与时间冲突；课程汇总显示该课程中最适合的 Section 资格结果，不能据此假设所有 Section 都匹配。DSC accommodation、校内工作、TA 季度数、candidacy 是人工审核信息，不自动排除学生。培训只有老师配置为必需时才影响资格匹配。

## CSV 导入顺序

管理员在左侧打开 **CSV Import / Export**：

1. 创建并选择一个明确的非历史招聘周期。
2. 选 **Courses & Sections**，选择新课程的 Organiser，填写 Department 及 **Hours per TA per week**（源文件没有工时列，因此必须填写）。选择课程 CSV，点击 **Preview import**，确认无错误后点击 **Import validated rows**。
3. 选 **Student Applications**，选择学生 CSV，预览并导入。
4. 选 **Selection & Assignment**，选择选拔 CSV，预览并导入。

一批内有错误时整批拒绝，不部分写入。重新上传完全相同的数据会显示 unchanged，不重复建账号或申请；有差异的同周期历史申请不会被覆盖。修改配置、文件或周期后须重新预览；预览之后其他人改动相关数据，也必须重新预览。

Organiser 只能导入自己课程的课程 CSV；学生资料和选拔结果由 Admin 导入。学生只能导出自己的申请。其他 staff 的导出遵循现有课程授权；无内部审核权限时导出不附内部备注。

新导入学生根据 UCI Email 和 Student ID 关联。新账号初始 suspended、随机未知密码，不创建共享默认密码，也不发邮件。需要让本人使用时，管理员在 Users 为每个账号设置独立密码并激活；既有学生账号的密码与账号状态不受导入影响。

## 字段和状态解释

- Student CSV 一人一行；Courses of Interest 多课程会生成该周期各课程的申请。历史不会合并到其他周期。
- `Testing Scenario / Purpose` 为测试来源说明，保留用于样本兼容，不要求学生填写。
- `Mass Spec` 与 `Mass Spectrometry` 做明确词汇统一；`Lab Section A` / `Lab A`、`Lab Section 1` / `Lab 1` 做明确 Section 别名匹配。模糊姓名、同名不同学生、重复职位或不明确课程均报错，不猜测身份。
- 时间支持 `Mon/Wed 09:00 - 10:20`、分号分开的多个时段及 `Fri All Day`。CSV 学生表仅提供冲突，没有完整可用时间；没有报告冲突时显示 Needs Confirmation，不能显示“已确认完整可用”。
- Skills & Experience 中的受控技能用于匹配，剩余经验文字保留为教学经历/补充说明；导出包含所选技能和经验文本。首次导出会统一部分别名和格式，不保证与原始文件逐字相同。
- 指定 Section 的批准只适用于该 Section；后续推荐不会覆盖已作出的批准/不选用决定。

- Selected → approve；Proposed → recommend；Flagged → flag；Rejected → not_selected；Alternate → alternate。拟分配 Section 与记录一起保存。导入 Selected 不创建已发布邀请、不伪造学生接受，不计入已接受工时；之后由 Organiser 走正常分配、发布、回复流程。
- Responsible User 保留为来源责任人文本；审计操作者始终是实际登录导入者。源文件 Match Status 不是权限或资格证明，导出使用系统当前资格/时间计算结果，可能与来源不同。

## 导出与历史

选择 Dataset 和周期，点击 **Export this cycle**。三类导出使用对应原文件表头。**Download template** 下载仅含表头的 UTF-8 CSV。导出为带 BOM 的 CSV，支持逗号、引号和换行；对可能执行公式的文本做防护。

学生表按周期每名学生输出最新提交的资料快照；单个历史申请的具体版本仍可在 Application Tracker / Review Workspace 展开查看。课程表输出当周期有效 Section；选拔表输出有明确拟分配 Section 的最新记录和分配台账，不把没有 Section 的课程级推荐猜成具体排班。历史缺失字段留空，不补造培训或确认状态。

原表头没有稳定的申请 ID 或版本号；跨系统合并时，同名学生不能安全自动关联。此时导入预览会拒绝，并要求先解决身份关联。课程工时由导入界面单独提供；跨周期回导课程 CSV 时需重新指定工时。

## 测试记录

- Java 单元/Servlet/真实 PostgreSQL 测试 342 项通过，无失败、错误或跳过。
- 原始样本 6 条课程 Section、20 名学生、6 条选拔记录均通过 HTTP 预览、导入、重复检测与导出；多课程兴趣按课程生成申请。
- 验证缺技能、明确时间冲突、All Day、本科 N/A、大小写/Section 别名、非法 CSV、整批回滚、课程范围和学生权限、预览过期。
- 验证导出后跨周期重新导入、数据库重连、原有快照和分配流程回归。
- 浏览器实际预览原始学生 CSV，显示 20 条 unchanged、0 个错误、未保存；确认新版姓名、培训、DSC、声明和技能复选框。
- HTTP 验证老师新增技能选项可供学生选择、未配置技能被拒绝、更新个人资料不改写旧申请、学生只能导出自己的资料。
