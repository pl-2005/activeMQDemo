# 多环境配置（Phase 6-1）

`phase2-springboot` 使用 **Spring Profile** 拆分环境，默认与本仓库此前行为一致：**`dev`**。

## 文件分工

| 文件 | 作用 |
|------|------|
| `application.yml` | 应用名、**默认激活的 profile**、各环境共用的 `demo.idempotency` |
| `application-dev.yml` | 本机 Docker：ActiveMQ / Redis / MySQL（原 `application.yml` 整段迁移至此） |
| `application-prod.yml` | 生产示例：**环境变量占位**，勿写真实密码 |
| `application-local.yml`（自建，已 gitignore） | 个人本机覆盖：密码、端口；需与 `dev` **组合**使用 |
| `application-local.yml.example` | 复制为 `application-local.yml` 的模板 |

## 默认启动

不传参数时等价于 `spring.profiles.active=dev`，连接 `localhost` 各服务。

## 激活其它环境

- **命令行**：`java -jar app.jar --spring.profiles.active=prod`
- **环境变量**：`SPRING_PROFILES_ACTIVE=prod`（Unix）或 Windows 下在启动前设置同名变量
- **IDE**：Run Configuration → Active profiles 填 `prod` 或 `dev,local`

## `local` profile（可选）

用于同事之间密码不一致又不想改 `application-dev.yml`：

1. 复制 `application-local.yml.example` 为 `application-local.yml`
2. 启动时使用 **`dev,local`**（**先 dev 后 local**），使本地文件覆盖 dev 中的连接字段

## 与后续主题的衔接

- **一致性说明**：不同环境的 **库名、队列名** 建议区分，避免误连生产 Broker 时与开发数据混用（`prod` 中 `DEMO_JMS_QUEUE_NAME` 可指向独立队列名）。
- **消息回放**：回放前务必确认 **当前 profile** 与 **目标队列** 指向正确 Broker。
