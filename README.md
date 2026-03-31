# 网约车派单调度仿真系统

基于 Spring Boot 的网约车派单调度仿真系统，模拟网约车平台的车辆调度、订单分配和路径规划。

## 功能特性

- 🚗 **车辆管理** - 车辆状态监控（履约中、巡游中、维护中）
- 🅿️ **停车场管理** - 南京真实停车场数据（新街口、德基、南京南站等）
- 📍 **站点管理** - 南京真实地理位置站点（商圈、景区、医院、社区、交通枢纽）
- 🎯 **订单调度** - 模拟订单派单和车辆调度
- 🗺️ **路径规划** - 高德/T3地图路线规划
- 🌐 **可视化界面** - 地图展示车辆和路线

## 技术栈

- **后端**: Spring Boot 3.2.0 + Java 17
- **数据库**: H2 内存数据库
- **地图服务**: 高德地图 API / T3 出行 API
- **构建工具**: Maven

## 快速开始

### 1. 运行项目

#### 方式一：使用 Maven
```bash
mvn spring-boot:run
```

#### 方式二：打包后运行
```bash
mvn clean package
java -jar target/ride-hailing-simulator-1.0-SNAPSHOT.jar
```

### 2. 访问应用

- 应用首页：http://localhost:8080
- H2 数据库控制台：http://localhost:8080/h2-console

## 数据说明

系统启动时会自动初始化以下数据：
- **10个停车场**：新街口金鹰、德基广场、南京南站、禄口机场、中山陵等
- **100辆车**：分布在各停车场，状态为"休息中"
- **100个站点**：覆盖商圈、景区、医院、社区、交通枢纽五类

## 项目结构

```
ride-hailing-simulator/
├── src/main/java/org/example/
│   ├── SimulateApplication.java          # Spring Boot 启动类
│   ├── config/
│   │   ├── WebConfig.java                # Web 配置
│   │   └── DataInitializer.java          # 数据初始化（已禁用）
│   ├── controller/
│   │   ├── OrderController.java          # 订单控制器
│   │   ├── ParkingLotController.java     # 停车场控制器
│   │   ├── StationController.java        # 站点控制器
│   │   └── VehicleController.java        # 车辆控制器
│   ├── entity/
│   │   ├── Order.java                    # 订单实体
│   │   ├── ParkingLot.java               # 停车场实体
│   │   ├── Station.java                  # 站点实体
│   │   └── Vehicle.java                  # 车辆实体
│   ├── repository/                       # 数据访问层
│   ├── service/                          # 业务逻辑层
│   └── dto/                              # 数据传输对象
├── src/main/resources/
│   ├── application.yml                   # 应用配置
│   ├── data.sql                          # 初始化数据脚本
│   └── static/                           # 前端页面
├── pom.xml                               # Maven 配置
└── README.md                             # 项目说明