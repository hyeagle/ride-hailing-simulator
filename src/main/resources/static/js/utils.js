/**
 * 工具函数模块
 */

// 状态颜色映射
const statusColors = {
    '巡游中': '#52c41a',
    '接乘中': '#13c2c2',
    '履约中': '#1890ff',
    '调度中': '#fa8c16',
    '维护中': '#faad14',
    '休息中': '#ff4d4f'
};

// 站点类型颜色映射
const stationTypeColors = {
    '商圈': '#ff6b6b',
    '景区': '#51cf66',
    '医院': '#339af0',
    '社区': '#845ef7',
    '交通枢纽': '#fcc419'
};

// 订单状态颜色映射
const orderStatusColors = {
    '待派单': '#1890ff',
    '待接乘': '#fa8c16',
    '进行中': '#52c41a',
    '已完成': '#8c8c8c',
    '已取消': '#ff4d4f'
};

/**
 * 获取状态对应的badge类名
 */
function getBadgeClass(status) {
    const map = {
        '巡游中': 'badge-info',
        '调度中': 'badge-warning',
        '接乘中': 'badge-warning',
        '履约中': 'badge-success',
        '维护中': 'badge-warning',
        '休息中': 'badge-danger',
        '待派单': 'badge-info',
        '待接乘': 'badge-warning',
        '进行中': 'badge-success',
        '已完成': 'badge-success',
        '已取消': 'badge-danger'
    };
    return map[status] || 'badge-info';
}

/**
 * 格式化时间
 */
function formatTime(time) {
    return time ? new Date(time).toLocaleString('zh-CN') : '-';
}

/**
 * 计算两点之间的距离（米）
 */
function calculateDistance(lat1, lng1, lat2, lng2) {
    const R = 6371000;
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLng = (lng2 - lng1) * Math.PI / 180;
    const a = Math.sin(dLat/2)**2 + Math.cos(lat1*Math.PI/180) * Math.cos(lat2*Math.PI/180) * Math.sin(dLng/2)**2;
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
}

// 导出全局变量供其他模块使用
window.statusColors = statusColors;
window.stationTypeColors = stationTypeColors;
window.orderStatusColors = orderStatusColors;
window.getBadgeClass = getBadgeClass;
window.formatTime = formatTime;
window.calculateDistance = calculateDistance;
