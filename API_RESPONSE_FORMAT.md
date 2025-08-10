# 线程池监控系统 - 统一响应格式文档

## 响应格式说明

所有API接口都使用统一的响应格式，包含以下字段：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1642694400000
}
```

### 字段说明

- `code`: 响应状态码（Integer）
- `message`: 响应消息（String）
- `data`: 响应数据（泛型，可为任意类型）
- `timestamp`: 时间戳（Long，毫秒）

## 状态码定义

### 成功状态码
- `200`: 操作成功

### 客户端错误状态码
- `400`: 请求参数错误
- `401`: 未授权
- `403`: 禁止访问
- `404`: 资源未找到
- `405`: 请求方法不允许

### 服务端错误状态码
- `500`: 服务器内部错误
- `503`: 服务不可用

### 业务错误状态码
- `1001`: 线程池未找到
- `1002`: 线程池配置错误
- `1003`: 告警配置错误
- `1004`: 指标收集错误
- `1005`: 时间范围参数错误

## API 示例

### 1. 获取所有线程池状态

**请求**
```
GET /api/threadpool/status
```

**成功响应**
```json
{
  "code": 200,
  "message": "获取线程池状态成功",
  "data": {
    "taskExecutor": {
      "poolName": "taskExecutor",
      "corePoolSize": 10,
      "maximumPoolSize": 20,
      "activeCount": 5,
      "queueSize": 100,
      "utilization": 50.0
    }
  },
  "timestamp": 1642694400000
}
```

### 2. 获取指定线程池状态

**请求**
```
GET /api/threadpool/status/taskExecutor
```

**成功响应**
```json
{
  "code": 200,
  "message": "获取线程池状态成功",
  "data": {
    "poolName": "taskExecutor",
    "corePoolSize": 10,
    "maximumPoolSize": 20,
    "activeCount": 5,
    "queueSize": 100,
    "utilization": 50.0
  },
  "timestamp": 1642694400000
}
```

**错误响应（线程池未找到）**
```json
{
  "code": 1001,
  "message": "线程池 'nonExistentPool' 未找到",
  "data": null,
  "timestamp": 1642694400000
}
```

### 3. 获取线程池历史指标

**请求**
```
GET /api/threadpool/metrics/taskExecutor?count=50
```

**成功响应**
```json
{
  "code": 200,
  "message": "获取线程池指标成功",
  "data": [
    {
      "timestamp": "2024-01-20T10:30:00",
      "activeCount": 5,
      "queueSize": 10,
      "utilization": 50.0
    }
  ],
  "timestamp": 1642694400000
}
```

**错误响应（参数错误）**
```json
{
  "code": 1002,
  "message": "查询数量必须在1-1000之间",
  "data": null,
  "timestamp": 1642694400000
}
```

### 4. 获取线程池统计信息

**请求**
```
GET /api/threadpool/statistics/taskExecutor?startTime=2024-01-20T00:00:00&endTime=2024-01-20T23:59:59
```

**成功响应**
```json
{
  "code": 200,
  "message": "获取线程池统计信息成功",
  "data": {
    "poolName": "taskExecutor",
    "avgUtilization": 65.5,
    "maxUtilization": 95.0,
    "minUtilization": 10.0,
    "totalTasks": 1000
  },
  "timestamp": 1642694400000
}
```

**错误响应（时间参数错误）**
```json
{
  "code": 1002,
  "message": "开始时间不能晚于结束时间",
  "data": null,
  "timestamp": 1642694400000
}
```

### 5. 配置告警规则

**请求**
```
POST /api/threadpool/alert/taskExecutor
Content-Type: application/json

{
  "utilizationThreshold": 80.0,
  "queueSizeThreshold": 100,
  "enabled": true
}
```

**成功响应**
```json
{
  "code": 200,
  "message": "告警配置已更新",
  "data": null,
  "timestamp": 1642694400000
}
```

### 6. 健康检查

**请求**
```
GET /api/threadpool/health
```

**成功响应**
```json
{
  "code": 200,
  "message": "健康检查完成",
  "data": {
    "status": "UP",
    "timestamp": "2024-01-20T10:30:00",
    "monitoredPools": ["taskExecutor", "scheduledExecutor"],
    "totalPools": 2,
    "unhealthyPools": 0,
    "healthScore": 100.0
  },
  "timestamp": 1642694400000
}
```

## 异常处理

系统提供全局异常处理，所有异常都会被转换为统一的响应格式：

1. **参数校验异常**: 返回400状态码和具体的校验错误信息
2. **业务异常**: 返回对应的业务错误码和错误信息
3. **系统异常**: 返回500状态码和通用错误信息

## 使用建议

1. 客户端应该根据`code`字段判断请求是否成功（200为成功）
2. 错误处理时应该显示`message`字段的内容给用户
3. `timestamp`字段可用于客户端缓存和调试
4. 所有时间参数使用ISO 8601格式：`yyyy-MM-ddTHH:mm:ss`