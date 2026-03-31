/**
 * 应用主入口
 */

// 全局变量
let map, orderDispatchMap;
let orderDispatchUpdateInterval = null;
let vehicleDispatchUpdateInterval = null;

// 初始化
document.addEventListener('DOMContentLoaded', () => {
    initMaps();
    loadStats();
    loadParkingLots();
    loadVehicles();
    loadStations();
    loadOrders();
    // 只加载一次数据，不循环调用dispatch/info接口
    loadOrderDispatchInfo();
    loadVehicleDispatchInfo();
    loadOverviewDispatchInfo();
    // 启动表格刷新定时器（只刷新显示，不调用后端）
    startVehicleDispatchUpdate();
    
    // 页面加载后默认显示车辆
    setTimeout(() => {
        showVehiclesOnMap();
    }, 500);
});

// 初始化地图
function initMaps() {
    map = new AMap.Map('map', { zoom: 11, center: [118.795, 32.05], viewMode: '2D' });
    orderDispatchMap = new AMap.Map('order-dispatch-map', { zoom: 11, center: [118.795, 32.05], viewMode: '2D' });
    
    // 导出到全局
    window.map = map;
    window.orderDispatchMap = orderDispatchMap;
}

// 加载统计数据
async function loadStats() {
    const [parking, vehicles, stations, orders, orderDispatch, vehicleDispatch] = await Promise.all([
        API.getParkingLots(),
        API.getVehicles(),
        API.getStations(),
        API.getOrders(),
        API.getOrderDispatchInfo(),
        API.getDispatchInfo()
    ]);
    
    document.getElementById('stat-parking').textContent = parking.length;
    document.getElementById('stat-vehicles').textContent = vehicles.length;
    document.getElementById('stat-stations').textContent = stations.length;
    document.getElementById('stat-orders').textContent = orders.length;
    
    const totalDispatch = orderDispatch.length + vehicleDispatch.length;
    document.getElementById('stat-dispatch').textContent = totalDispatch;
}

// 切换面板
function showPanel(panelId) {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
    event.target.classList.add('active');
    document.getElementById(panelId).classList.add('active');
}

// 导出全局函数
window.initMaps = initMaps;
window.loadStats = loadStats;
window.showPanel = showPanel;
