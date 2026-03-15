# jmcomic-parent

> 基于 Spring Boot 3.4.6 + Spring Cloud 2024.0.0 JMComic解析服务

## 免责声明

本项目仅供学习与研究 Spring Boot 微服务、响应式编程、AOP 等技术使用。请勿将本项目用于任何商业用途或违反目标网站服务条款的行为。使用本项目产生的一切法律责任由使用者自行承担。

---

## 项目架构

```
jmcomic-parent
├── eureka-server        # Eureka 服务注册中心 (端口 7891)
├── jmcomic-api          # 主工作节点：网页爬取、图片处理、加密API (端口 8091)
├── jmcomic-gateway      # API 网关，负载均衡路由 (端口 7813)
└── jmcomic-common       # 共享 POJO/DTO/枚举（无 Spring 依赖）
```

**核心技术栈**

| 类别 | 技术 |
|------|------|
| 语言 | Java 17 |
| 框架 | Spring Boot 3.4.6 + Spring Cloud 2024.0.0 |
| 响应式 | WebFlux (WebClient, Reactor) |
| 服务发现 | Netflix Eureka |
| HTML 解析 | Jsoup 1.17.2 |
| JSON | Fastjson 2.0.50 |
| 图片处理 | Scrimage 4.2.0 + WebP |
| AOP | Spring AOP (AspectJ) |
| 构建工具 | Maven (Maven Wrapper 3.9.7) |

---

## 模块说明

### eureka-server
服务注册中心，其他模块均向其注册并通过它发现服务。

### jmcomic-api
核心业务模块，分为两个子版本：

- **v1** — 通过 Jsoup 直接爬取 Web 页面，解析 HTML 获取漫画/章节/评论数据
- **v2** — 调用加密 API，请求需生成 MD5 签名，响应需 AES 解密

**主要功能**

| 功能 | 说明 |
|------|------|
| 漫画搜索 / 详情 | 搜索漫画列表、获取漫画元数据 |
| 章节信息 | 获取章节图片列表、图片文件名 |
| 图片处理 | 下载并解码混淆图片，支持加水印 |
| 小说接口 (v2) | 小说搜索、列表、详情、章节 |
| 评论 | 分页获取漫画/章节评论与嵌套回复 |
| 定时任务 | 站点/CDN 连通性探测、自动登录 |

### jmcomic-gateway
面向外部的 API 网关，通过 Eureka + Spring Cloud LoadBalancer 代理请求到 `jmcomic-api`。

**路由结构**

| 前缀 | 对应 API |
|------|---------|
| `/album` | v1 漫画接口 |
| `/chapter` | v1 章节接口 |
| `/image` | v1 图片接口 |
| `/v2/album` | v2 漫画接口 |
| `/v2/chapter` | v2 章节接口 |
| `/v2/novel` | v2 小说接口 |

### jmcomic-common
纯 Java 库，不依赖 Spring，提供模块间共享的数据模型：

- **Record POJO**：`AlbumInfo`、`AlbumDetail`、`ChapterInfo`、`Comment`、`AlbumPage<T>`
- **DTO**：`SearchDto`、`ChapterImageDto`、`CommentQueryDataDto`
- **枚举**：`AlbumType`、`AlbumTag`、`SortType`、`AlbumTime`

---

## 关键设计

### 站点故障转移（v1）
`WebCrawlFactory` 维护一个站点列表，按顺序轮询。`SiteRegistry` 实时跟踪每个站点的存活状态，定时任务（`WebsiteHttpingTask`）异步 ping 并更新状态。请求失败时自动切换下一个站点。

### CDN 轮询重试
`ImageDownloadClient` 轮询 CDN 列表，单次下载失败自动切换，最多重试 `maxRetry` 次（默认 3 次）。

### 速率限制
`WebCrawlClient` 使用 `ReentrantLock` 保证线程安全，每 60 秒最多发出 30 个请求。

### v2 签名 + 解密（AOP）
`@SecureApi` 注解配合 `SecureHttpClientAspect`（`@Around`）：

1. **前置**：生成签名并注入到请求头
   - `Token` = MD5(unixSeconds + clientToken)
   - `Tokenparam` = unixSeconds + "," + clientVersion
2. **后置**：在 Reactor `Mono` 流中对密文做 AES 解密

### 开发环境支持
`DevConfig` 检测 `DevProperties` 是否配置：若配置了开发 cookies，则跳过登录逻辑并直接使用；支持 HTTP 代理配置（`jmcomic.v1.dev.proxy`）。

---

## 配置说明

`application.yaml` 中所有敏感信息（账号、密码、token）均不包含在版本库中，需自行填写。

### jmcomic-api `application.yaml` 关键配置

```yaml
jmcomic:
  v1:
    login:
      username: ""          # 填写账号
      password: ""          # 填写密码
      relogin-day: 180      # 每隔多少天重新登录(没实现,目前定时周一)
    website:
      httping:
        interval: 300000    # 站点探测间隔(ms)
        timeout: 5000
      rate-limit:
        enabled: true
        minute: 30          # 每分钟最多请求数
      max-retry: 3
    image:
      httping:
        interval: 600000    # CDN 探测间隔(ms)
      max-retry: 3
      watermark:
        enabled: false
  v2:
    setting:
      token: ""             # 填写 API token
      version: ""           # 填写客户端版本号
      api:
        max-retry: 3
        timeout: 10000
```

### 服务地址

| 服务 | 地址 |
|------|------|
| Eureka Dashboard | http://localhost:7891 |
| API 节点 | http://localhost:8091 |
| 网关 | http://localhost:7813 |

---

## 构建与运行

### 环境要求
- JDK 17+
- Maven（通过 `mvnw` wrapper，无需手动安装）

### 构建命令

```bash
# 构建所有模块
./mvnw clean package -DskipTests

# 构建特定模块
./mvnw clean package -pl jmcomic-api -DskipTests
./mvnw clean package -pl jmcomic-gateway -DskipTests
```

### 启动顺序

```bash
# 1. 先启动 Eureka 注册中心
java -jar eureka-server/target/eureka-server-*.jar

# 2. 启动 API 工作节点
java -jar jmcomic-api/target/jmcomic-api-*-exec.jar

# 3. 启动网关（可选，用于统一路由）
java -jar jmcomic-gateway/target/jmcomic-gateway-*-exec.jar
```

---

## API 接口

### v1 接口（`/jmapi/v1`）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/album/search` | 搜索漫画 |
| GET | `/album/{id}` | 漫画详情 |
| GET | `/chapter/{id}` | 章节图片列表 |
| POST | `/chapter/comment` | 章节评论（分页） |
| POST | `/image/decode` | 解码混淆图片 |
| GET | `/image/gif/{chapterId}/{fileName}` | 下载 GIF |
| GET | `/image/cover/{albumId}` | 漫画封面 |
| GET | `/image/avatar/{filename}` | 用户头像 |

### v2 接口（`/jmapi/v2`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/album/search` | 搜索漫画 |
| GET | `/album/{id}` | 漫画详情 |
| GET | `/album/hot-tags` | 热门标签 |
| GET | `/album/random-recommend` | 随机推荐 |
| GET | `/album/week` | 本周排行 |
| GET | `/chapter/{id}` | 章节信息 |
| GET | `/chapter/read/{id}` | 章节阅读 |
| GET | `/chapter/{id}/comment/{page}` | 章节评论（分页） |
| GET | `/novel/search` | 小说搜索 |
| GET | `/novel/list` | 小说列表 |
| GET | `/novel/{nid}` | 小说详情 |
| GET | `/novel/chapter/{cid}` | 小说章节 |

---

## 项目目录结构

```
jmcomic-api/src/main/java/com/lazy/jmcomic/api/
├── common/
│   ├── annotation/     # @JMController.v1/.v2, @JMValue
│   └── util/           # HttpPingUtil
├── v1/
│   ├── client/         # WebCrawlClient, WebCrawlFactory, ImageDownloadClient
│   ├── config/         # WebClientConfig, SiteRegistry, SiteCookie, DevConfig, FontConfig
│   │   ├── bean/       # Watermark, TodayDate
│   │   └── properties/ # WebsiteProperties, ImageProperties, LoginProperties, DevProperties
│   ├── constant/
│   │   ├── selector/   # CSS 选择器常量
│   │   └── fragment/   # HTML 片段选择器
│   ├── controller/     # AlbumController, ChapterController, ImageController
│   ├── service/        # IAlbumService, IChapterService, ImageService 及实现类
│   └── task/           # WebsiteHttpingTask, ImageHttpPingTask, WebsiteLoginTask
└── v2/
    ├── annotation/     # @SecureApi
    ├── aspect/         # SecureHttpClientAspect
    ├── client/         # ApiClient, ApiClientFactory
    ├── config/         # ApiDomainRegistry
    │   └── properties/ # SettingProperties
    ├── constant/       # ApiPath, ApiRequest, Properties
    ├── controller/     # AlbumController, ChapterController, NovelController
    ├── crypto/
    │   ├── sign/       # SignatureGenerator
    │   └── decrypt/    # ResponseDecryptor
    ├── pojo/           # ApiRequest
    └── task/           # ApiHttpingTask
```

---

## License

[MIT](LICENSE)