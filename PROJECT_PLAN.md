# 银盾守护 - 项目规划文档

> 本文是早期产品构想，所列能力、性能指标、方言数量和机构协同均为规划目标，并非当前源码已经实现或验证的功能。请以 [实现状态说明](docs/实现状态.md) 为准。

## 1. 项目概述

**项目名称**: 银盾守护  
**目标平台**: Android (APK)  
**核心定位**: 面向老年人的全方位防诈骗与财务安全守护应用  

### 核心特性
- 完全离线可用，内置AI模型
- 即开即用，无需复杂配置
- 适老化交互：128种方言支持、大字体、单路径操作
- 0.3秒极速识别
- "老人+子女+社区+公安"四方协同

---

## 2. 技术架构

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        银盾守护 App                              │
├─────────────────────────────────────────────────────────────────┤
│  表现层 (UI Layer)                                               │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌────────────┐ │
│  │ 财务监控模块 │ │ 模拟训练模块 │ │ 通话防护模块 │ │ 报案追踪模块│ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│  业务逻辑层 (Domain Layer)                                       │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌────────────┐ │
│  │ 风险评估引擎 │ │ AI识别引擎   │ │ 对话管理器  │ │ 数据聚合器 │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│  数据层 (Data Layer)                                             │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌────────────┐ │
│  │ 本地数据库   │ │ AI模型库    │ │ 加密存储    │ │ 同步引擎   │ │
│  │ (SQLite)    │ │ (ONNX/TFLite)│ │ (Encrypted) │ │           │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│  系统层 (System Layer)                                           │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌────────────┐ │
│  │ 通话监听服务 │ │ TTS引擎      │ │ 通知系统    │ │ 权限管理   │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 技术选型

| 层次 | 技术选择 | 说明 |
|------|---------|------|
| **UI框架** | Jetpack Compose | 现代声明式UI，更好适老化支持 |
| **语言** | Kotlin | Android官方推荐语言 |
| **数据库** | Room (SQLite) | 本地持久化存储 |
| **AI推理** | ONNX Runtime Mobile | 离线AI模型运行 |
| **语音识别** | 内置方言模型 + 系统API | 128种方言支持 |
| **TTS** | Android TTS + 自定义语音库 | 模拟诈骗话术训练 |
| **加密** | Android Keystore + AES-256 | 本地数据加密 |
| **通话监听** | AccessibilityService | 来电监听与风险检测 |
| **数据同步** | WorkManager | 后台同步，更新话术库 |

---

## 3. 模块设计

### 3.1 财务异常行为监控模块

**功能清单**:
- 银行卡绑定与流水获取
- 微信钱包绑定与数据读取
- 异常交易识别 (高额小额转账、大额非常规支出)
- 实时预警 (语音提醒 + 通知紧急联系人)

**数据库表**:
```sql
-- 账户表
CREATE TABLE accounts (
    id INTEGER PRIMARY KEY,
    user_id INTEGER,
    bank_name TEXT,
    account_number_encrypted TEXT,
    account_type TEXT,  -- 'bank_card' | 'wechat' | 'alipay'
    created_at TIMESTAMP
);

-- 交易记录表
CREATE TABLE transactions (
    id INTEGER PRIMARY KEY,
    account_id INTEGER,
    amount DECIMAL(15,2),
    counterparty TEXT,
    description TEXT,
    transaction_time TIMESTAMP,
    is_suspicious BOOLEAN,
    risk_score INTEGER
);

-- 风险规则表
CREATE TABLE risk_rules (
    id INTEGER PRIMARY KEY,
    rule_name TEXT,
    rule_type TEXT,  -- 'amount_threshold' | 'frequency' | 'pattern'
    parameters JSON,
    risk_level INTEGER,
    is_active BOOLEAN
);
```

### 3.2 模拟诈骗话术训练模块

**功能清单**:
- 诈骗类型分类 (保健品、投资理财、冒充公检法等)
- 语音交互模拟 (TTS生成诈骗电话)
- 关键词识别与提示 ("高额回报"、"安全账户")
- 训练后反馈与建议
- 防骗红包奖励系统

**数据库表**:
```sql
-- 诈骗话术库
CREATE TABLE fraud_scripts (
    id INTEGER PRIMARY KEY,
    category TEXT,  -- 'health_product' | 'investment' | 'impersonate' | ...
    title TEXT,
    script_content JSON,  -- 包含对话流程
    keywords JSON,
    risk_level INTEGER,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 训练记录
CREATE TABLE training_records (
    id INTEGER PRIMARY KEY,
    user_id INTEGER,
    script_id INTEGER,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    score INTEGER,  -- 识别准确度
    identified_keywords JSON,
    reward_amount DECIMAL(10,2)
);

-- 积分账户
CREATE TABLE points_account (
    id INTEGER PRIMARY KEY,
    user_id INTEGER,
    balance DECIMAL(10,2),
    total_earned DECIMAL(10,2),
    total_spent DECIMAL(10,2)
);
```

### 3.3 陌生来电监听模块

**功能清单**:
- 来电风险分级检测
- 白名单管理
- 低风险弹窗提醒
- 高风险强制中断 + 语音警告

**数据库表**:
```sql
-- 通话记录
CREATE TABLE call_logs (
    id INTEGER PRIMARY KEY,
    phone_number TEXT,
    call_time TIMESTAMP,
    duration INTEGER,
    risk_level INTEGER,
    risk_reason TEXT,
    is_whitelisted BOOLEAN
);

-- 白名单
CREATE TABLE whitelist (
    id INTEGER PRIMARY KEY,
    user_id INTEGER,
    phone_number TEXT,
    contact_name TEXT,
    relationship TEXT  -- 'child' | 'community' | 'volunteer'
);

-- 号码风险库
CREATE TABLE phone_risk_db (
    id INTEGER PRIMARY KEY,
    phone_number TEXT,
    risk_type TEXT,
    report_count INTEGER,
    last_report_time TIMESTAMP
);
```

### 3.4 诈骗受理与追踪模块

**功能清单**:
- 用户自主提交报案
- 子女代为提交线索
- 证据材料上传
- 自动PDF生成
- 案件进度可视化
- 96110一键拨打

**数据库表**:
```sql
-- 案件表
CREATE TABLE cases (
    id INTEGER PRIMARY KEY,
    case_number TEXT UNIQUE,  -- 案件编号
    reporter_id INTEGER,
    reporter_type TEXT,  -- 'elderly' | 'child'
    case_type TEXT,
    amount DECIMAL(15,2),
    suspect_info JSON,
    description TEXT,
    status TEXT,  -- 'submitted' | 'processing' | 'resolved'
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 证据材料
CREATE TABLE evidence (
    id INTEGER PRIMARY KEY,
    case_id INTEGER,
    evidence_type TEXT,  -- 'recording' | 'screenshot' | 'transfer_record'
    file_path TEXT,
    file_hash TEXT,
    uploaded_at TIMESTAMP
);

-- 案件进度
CREATE TABLE case_progress (
    id INTEGER PRIMARY KEY,
    case_id INTEGER,
    status TEXT,
    description TEXT,
    operator TEXT,
    timestamp TIMESTAMP
);
```

### 3.5 诈骗数据收集平台

**功能清单**:
- 用户提交诈骗经历
- 匿名制保护
- 风险评估
- 诈骗电话所属地/风险/等级查询

**数据库表**:
```sql
-- 诈骗报告
CREATE TABLE fraud_reports (
    id INTEGER PRIMARY KEY,
    phone_number TEXT,
    fraud_type TEXT,
    fraud_content TEXT,
    location TEXT,
    report_time TIMESTAMP,
    is_anonymous BOOLEAN
);

-- 诈骗电话库
CREATE TABLE fraud_phone_db (
    id INTEGER PRIMARY KEY,
    phone_number TEXT,
    location TEXT,
    fraud_type TEXT,
    risk_level INTEGER,
    report_count INTEGER,
    last_report_time TIMESTAMP
);
```

---

## 4. 目录结构

```
银盾守护/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/yindun/shouhu/
│   │   │   │   ├── di/                    # 依赖注入
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/             # Room数据库、DAO
│   │   │   │   │   ├── remote/            # API接口（如有）
│   │   │   │   │   └── repository/        # 数据仓库
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/             # 数据模型
│   │   │   │   │   ├── usecase/           # 业务用例
│   │   │   │   │   └── engine/            # AI引擎、风险引擎
│   │   │   │   ├── ui/
│   │   │   │   │   ├── theme/             # 主题、适老化样式
│   │   │   │   │   ├── navigation/        # 导航
│   │   │   │   │   ├── financial/         # 财务监控界面
│   │   │   │   │   ├── training/          # 模拟训练界面
│   │   │   │   │   ├── call/              # 通话防护界面
│   │   │   │   │   ├── report/            # 报案追踪界面
│   │   │   │   │   ├── family/            # 子女守护界面
│   │   │   │   │   └── settings/          # 设置界面
│   │   │   │   ├── service/               # 后台服务
│   │   │   │   └── util/                  # 工具类
│   │   │   ├── assets/
│   │   │   │   ├── models/                # ONNX AI模型
│   │   │   │   ├── scripts/               # 诈骗话术库
│   │   │   │   ├── audio/                 # TTS语音资源
│   │   │   │   └── fonts/                 # 大字体资源
│   │   │   └── res/
│   │   └── test/
│   └── build.gradle.kts
├── models/                                 # AI模型训练与导出
├── scripts/                               # 话术库维护脚本
├── docs/                                  # 文档
└── build.gradle.kts
```

---

## 5. 开发阶段规划

### 阶段一：基础框架搭建 (第1-2周)
- [x] 创建Android项目结构
- [ ] 配置Gradle依赖
- [ ] 实现Room数据库基础架构
- [ ] 搭建基础UI框架和适老化主题
- [ ] 实现导航框架

### 阶段二：核心AI引擎 (第3-4周)
- [ ] 集成ONNX Runtime
- [ ] 实现诈骗话术识别模型
- [ ] 实现交易异常检测模型
- [ ] 模型量化与优化

### 阶段三：功能模块实现 (第5-8周)
- [ ] 财务监控模块
- [ ] 模拟训练模块
- [ ] 通话防护模块
- [ ] 报案追踪模块

### 阶段四：数据收集平台 (第9-10周)
- [ ] 诈骗数据收集功能
- [ ] 匿名化处理
- [ ] 风险评估系统

### 阶段五：集成测试与优化 (第11-12周)
- [ ] 功能测试
- [ ] 性能优化
- [ ] 适老化测试
- [ ] 打包发布

---

## 6. 性能指标

| 指标 | 目标值 |
|------|--------|
| 诈骗话术识别速度 | ≤0.3秒 |
| 交易异常检测速度 | ≤0.5秒 |
| AI模型大小 | ≤50MB |
| 应用启动时间 | ≤2秒 |
| 离线可用性 | 100%核心功能 |
| 识别准确率 | ≥90% |

---

## 7. 安全与隐私

### 数据安全
- 本地存储使用AES-256加密
- 使用Android Keystore管理密钥
- 敏感信息不在日志中输出

### 隐私保护
- 数据仅用于防诈骗服务，不用于商业用途
- 用户可随时解绑账号并删除数据
- 数据收集平台采用匿名制

### 权限管理
- 最小权限原则
- 敏感权限需明确授权
- 用户可随时关闭权限

---

## 8. 后续扩展

### 短期扩展
- 社区志愿者接入
- 多语言界面
- 数据备份与恢复

### 中期扩展
- 小程序版本
- 语音硬件终端
- 反诈保险对接

### 长期扩展
- 联邦学习能力
- 更多方言支持
- 智能家居集成
