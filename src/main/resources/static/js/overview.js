/**
 * 概览页地图模块
 */

let selectedTypes = new Set();
let overviewMarkers = [];
let overviewDispatchVehicles = {};
let overviewDispatchAnimationTimer = null;
let overviewDispatchUpdateInterval = null;

// 清除概览地图上的标记
function clearOverviewMarkers() {
    overviewMarkers.forEach(m => window.map.remove(m));
    overviewMarkers = [];
}

// 高亮卡片（多选）
function highlightCard(type) {
    if (selectedTypes.has(type)) {
        selectedTypes.delete(type);
    } else {
        selectedTypes.add(type);
    }
    document.querySelectorAll('.stat-card').forEach(c => c.classList.remove('active'));
    selectedTypes.forEach(t => {
        document.getElementById('card-' + t)?.classList.add('active');
    });
}

// 刷新地图显示
async function refreshOverviewMap() {
    hideAllFilters();
    
    const promises = [];
    if (selectedTypes.has('parking')) promises.push(API.getParkingLots());
    else promises.push(Promise.resolve(null));
    
    if (selectedTypes.has('vehicles')) promises.push(API.getVehicles());
    else promises.push(Promise.resolve(null));
    
    if (selectedTypes.has('stations')) promises.push(API.getStations());
    else promises.push(Promise.resolve(null));
    
    if (selectedTypes.has('orders')) {
        promises.push(API.getOrders());
        promises.push(API.getStations());
    } else {
        promises.push(Promise.resolve(null));
        promises.push(Promise.resolve(null));
    }
    
    const [parkingData, vehicleData, stationData, orderData, orderStationData] = await Promise.all(promises);
    
    const typesToRefresh = ['parking', 'vehicles', 'stations', 'orders'];
    typesToRefresh.forEach(type => {
        const oldMarkers = overviewMarkers.filter(m => m.getExtData?.() === type);
        oldMarkers.forEach(m => window.map.remove(m));
        overviewMarkers = overviewMarkers.filter(m => m.getExtData?.() !== type);
    });
    
    if (selectedTypes.has('parking') && parkingData) {
        renderParkingLotMarkers(parkingData);
    }
    if (selectedTypes.has('vehicles') && vehicleData) {
        const selectedStatuses = getSelectedVehicleStatuses();
        const filteredVehicles = vehicleData.filter(v => selectedStatuses.includes(v.status));
        renderVehicleMarkers(filteredVehicles, false);
        document.getElementById('vehicle-map-filter').classList.add('show');
    }
    if (selectedTypes.has('stations') && stationData) {
        renderStationMarkers(stationData.slice(0, 200));
        document.getElementById('station-map-filter').classList.add('show');
    }
    if (selectedTypes.has('orders') && orderData && orderStationData) {
        renderOrderMarkers(orderData, orderStationData);
        document.getElementById('order-map-filter').classList.add('show');
    }
}

// 渲染停车场标记
function renderParkingLotMarkers(data) {
    data.forEach(p => {
        const marker = new AMap.Marker({
            position: [p.longitude, p.latitude],
            content: `<div style="width: 24px; height: 24px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 50% 50% 50% 0; transform: rotate(-45deg); box-shadow: 0 2px 6px rgba(0,0,0,0.3); border: 2px solid white; display: flex; align-items: center; justify-content: center;">
                <span style="transform: rotate(45deg); font-size: 10px;">🅿️</span>
            </div>`,
            offset: new AMap.Pixel(-12, -24),
            title: `${p.name}\n容量: ${p.capacity}\n网格: ${p.gisGrid}`
        });
        marker.getExtData = () => 'parking';
        window.map.add(marker);
        overviewMarkers.push(marker);
    });
}

// 显示停车场位置
async function showParkingLotsOnMap() {
    highlightCard('parking');
    await refreshOverviewMap();
}

// 显示车辆位置
async function showVehiclesOnMap() {
    highlightCard('vehicles');
    await refreshOverviewMap();
}

// 获取选中的状态列表
function getSelectedVehicleStatuses() {
    const statuses = [];
    const allStatuses = ['巡游中', '调度中', '接乘中', '履约中', '维护中', '休息中'];
    allStatuses.forEach(s => {
        const checkbox = document.getElementById('filter-' + s);
        if (checkbox && checkbox.checked) {
            statuses.push(s);
        }
    });
    return statuses;
}

// 多选状态筛选车辆
async function filterVehiclesByStatus() {
    if (!selectedTypes.has('vehicles')) {
        selectedTypes.add('vehicles');
        document.getElementById('card-vehicles')?.classList.add('active');
    }
    await refreshOverviewMap();
}

// 全选状态
function selectAllVehicleStatus() {
    ['巡游中', '调度中', '接乘中', '履约中', '维护中', '休息中'].forEach(s => {
        const checkbox = document.getElementById('filter-' + s);
        if (checkbox) checkbox.checked = true;
    });
    filterVehiclesByStatus();
}

// 全不选状态
function deselectAllVehicleStatus() {
    ['巡游中', '调度中', '接乘中', '履约中', '维护中', '休息中'].forEach(s => {
        const checkbox = document.getElementById('filter-' + s);
        if (checkbox) checkbox.checked = false;
    });
    filterVehiclesByStatus();
}

// 渲染车辆标记
function renderVehicleMarkers(data, autoFit = true) {
    data.forEach(v => {
        if (overviewDispatchVehicles[String(v.id)]) {
            return;
        }
        
        const color = statusColors[v.status] || '#f5576c';
        const marker = new AMap.Marker({
            position: [v.longitude, v.latitude],
            content: `<div style="width: 20px; height: 20px; background: ${color}; border-radius: 50%; box-shadow: 0 2px 6px rgba(0,0,0,0.3); border: 2px solid white; display: flex; align-items: center; justify-content: center;">
                <span style="font-size: 8px;">🚗</span>
            </div>`,
            offset: new AMap.Pixel(-10, -10),
            title: `车辆${v.id}\nVIN: ${v.vin}\n状态: ${v.status}\n电量: ${v.batteryPercent}%\n订单: ${v.orderId || '无'}`
        });
        marker.getExtData = () => 'vehicles';
        window.map.add(marker);
        overviewMarkers.push(marker);
    });
}

// 显示站点位置
async function showStationsOnMap() {
    highlightCard('stations');
    await refreshOverviewMap();
}

// 按类型筛选显示站点
async function showStationsOnMapByType(type) {
    if (!selectedTypes.has('stations')) {
        selectedTypes.add('stations');
        document.getElementById('card-stations')?.classList.add('active');
    }
    
    const oldMarkers = overviewMarkers.filter(m => m.getExtData?.() === 'stations');
    oldMarkers.forEach(m => window.map.remove(m));
    overviewMarkers = overviewMarkers.filter(m => m.getExtData?.() !== 'stations');
    
    let data;
    if (type) {
        data = await API.getStationsByType(type);
    } else {
        data = await API.getStations();
    }
    
    renderStationMarkers(data.slice(0, 200));
}

// 渲染站点标记
function renderStationMarkers(data) {
    data.forEach(s => {
        const color = stationTypeColors[s.type] || '#4facfe';
        const marker = new AMap.Marker({
            position: [s.longitude, s.latitude],
            content: `<div style="width: 18px; height: 18px; background: ${color}; border-radius: 50%; box-shadow: 0 2px 6px rgba(0,0,0,0.3); border: 2px solid white;"></div>`,
            offset: new AMap.Pixel(-9, -9),
            title: `${s.name}\n类型: ${s.type}\n备注: ${s.remark || '无'}`
        });
        marker.getExtData = () => 'stations';
        window.map.add(marker);
        overviewMarkers.push(marker);
    });
}

// 显示订单位置
async function showOrdersOnMap() {
    highlightCard('orders');
    await refreshOverviewMap();
}

// 按状态筛选显示订单
async function showOrdersOnMapByStatus(status) {
    if (!selectedTypes.has('orders')) {
        selectedTypes.add('orders');
        document.getElementById('card-orders')?.classList.add('active');
    }
    
    const oldMarkers = overviewMarkers.filter(m => m.getExtData?.() === 'orders');
    oldMarkers.forEach(m => window.map.remove(m));
    overviewMarkers = overviewMarkers.filter(m => m.getExtData?.() !== 'orders');
    
    let data;
    if (status) {
        data = await API.getOrdersByStatus(status);
    } else {
        data = await API.getOrders();
    }
    
    const stations = await API.getStations();
    renderOrderMarkers(data, stations);
}

// 渲染订单标记
function renderOrderMarkers(data, stations) {
    const stationMap = {};
    stations.forEach(s => stationMap[s.id] = s);
    
    data.forEach(o => {
        const originStation = stationMap[o.originStationId];
        const destStation = stationMap[o.destinationStationId];
        if (!originStation) return;
        
        const color = orderStatusColors[o.status] || '#43e97b';
        const marker = new AMap.Marker({
            position: [originStation.longitude, originStation.latitude],
            content: `<div style="width: 16px; height: 16px; background: ${color}; border-radius: 50%; box-shadow: 0 2px 6px rgba(0,0,0,0.3); border: 2px solid white; position: relative;">
                <div style="position: absolute; top: -2px; right: -2px; width: 6px; height: 6px; background: white; border-radius: 50%; border: 1px solid ${color};"></div>
            </div>`,
            offset: new AMap.Pixel(-8, -8),
            title: `订单${o.id}\n状态: ${o.status}\n起点: ${originStation.name || o.originStationId}\n终点: ${destStation ? destStation.name : o.destinationStationId}\n收益: ¥${o.estimatedRevenue.toFixed(2)}`
        });
        marker.getExtData = () => 'orders';
        window.map.add(marker);
        overviewMarkers.push(marker);
    });
}

// 隐藏所有筛选栏
function hideAllFilters() {
    document.getElementById('vehicle-map-filter').classList.remove('show');
    document.getElementById('station-map-filter').classList.remove('show');
    document.getElementById('order-map-filter').classList.remove('show');
}

// ===== 概览页面调度动画 =====

function startOverviewDispatchAnimation() {
    loadOverviewDispatchInfo();
    overviewDispatchUpdateInterval = setInterval(() => {
        loadOverviewDispatchInfo();
    }, 3000);
}

async function loadOverviewDispatchInfo() {
    try {
        const data = await API.getDispatchInfo();
        syncOverviewDispatchVehicles(data);
    } catch (error) {
        console.error('加载概览调度信息失败', error);
    }
}

function removeVehicleMarkerFromOverview(vehicleId) {
    const markersToRemove = [];
    overviewMarkers.forEach((marker, index) => {
        if (marker.getExtData && marker.getExtData()) {
            const extData = marker.getExtData();
            if (String(extData.vehicleId) === String(vehicleId)) {
                markersToRemove.push(index);
                window.map.remove(marker);
            }
        }
    });
    markersToRemove.reverse().forEach(index => {
        overviewMarkers.splice(index, 1);
    });
}

function syncOverviewDispatchVehicles(data) {
    const currentVehicleIds = new Set(Object.keys(overviewDispatchVehicles));
    const newVehicleIds = new Set(data.map(d => d.vehicleId));
    
    currentVehicleIds.forEach(id => {
        if (!newVehicleIds.has(id)) {
            const vehicle = overviewDispatchVehicles[id];
            if (vehicle.marker) window.map.remove(vehicle.marker);
            if (vehicle.polyline) window.map.remove(vehicle.polyline);
            if (vehicle.destMarker) window.map.remove(vehicle.destMarker);
            if (vehicle.originMarker) window.map.remove(vehicle.originMarker);
            delete overviewDispatchVehicles[id];
        }
    });
    
    data.forEach(d => {
        if (!overviewDispatchVehicles[d.vehicleId] && d.path && d.path.length > 0) {
            removeVehicleMarkerFromOverview(d.vehicleId);
            
            const marker = new AMap.Marker({
                position: [d.path[0][0], d.path[0][1]],
                content: `<div style="width: 20px; height: 20px; background: #fa8c16; border-radius: 50%; box-shadow: 0 2px 6px rgba(0,0,0,0.3); border: 2px solid white; display: flex; align-items: center; justify-content: center; cursor: pointer;">
                    <span style="font-size: 10px;">🚗</span>
                </div>`,
                offset: new AMap.Pixel(-10, -10),
                zIndex: 100,
                extData: { vehicleId: d.vehicleId, type: 'dispatch' }
            });
            window.map.add(marker);
            
            marker.on('click', function() {
                toggleDispatchRoute(d.vehicleId);
            });
            
            overviewDispatchVehicles[d.vehicleId] = {
                marker,
                polyline: null,
                destMarker: null,
                originMarker: null,
                path: d.path,
                targetLongitude: d.targetLongitude,
                targetLatitude: d.targetLatitude,
                targetStationName: d.targetStationName,
                originLongitude: d.path[0][0],
                originLatitude: d.path[0][1],
                progress: 0,
                animating: true,
                routeVisible: false,
                vehicleId: d.vehicleId
            };
        }
    });
    
    if (!overviewDispatchAnimationTimer && Object.keys(overviewDispatchVehicles).length > 0) {
        startOverviewDispatchAnimationLoop();
    }
}

function toggleDispatchRoute(vehicleId) {
    const vehicle = overviewDispatchVehicles[vehicleId];
    if (!vehicle) return;
    
    if (vehicle.routeVisible) {
        if (vehicle.polyline) {
            window.map.remove(vehicle.polyline);
            vehicle.polyline = null;
        }
        if (vehicle.destMarker) {
            window.map.remove(vehicle.destMarker);
            vehicle.destMarker = null;
        }
        if (vehicle.originMarker) {
            window.map.remove(vehicle.originMarker);
            vehicle.originMarker = null;
        }
        vehicle.routeVisible = false;
    } else {
        vehicle.polyline = new AMap.Polyline({
            path: vehicle.path,
            strokeColor: '#fa8c16',
            strokeWeight: 4,
            strokeOpacity: 0.7,
            strokeStyle: 'dashed'
        });
        window.map.add(vehicle.polyline);
        
        vehicle.originMarker = new AMap.Marker({
            position: [vehicle.originLongitude, vehicle.originLatitude],
            content: `<div style="background: #1890ff; color: white; padding: 3px 6px; border-radius: 4px; font-size: 10px; white-space: nowrap; box-shadow: 0 2px 4px rgba(0,0,0,0.2);">
                📍 起点
            </div>`,
            offset: new AMap.Pixel(-10, -10),
            zIndex: 99
        });
        window.map.add(vehicle.originMarker);
        
        if (vehicle.targetLongitude && vehicle.targetLatitude) {
            vehicle.destMarker = new AMap.Marker({
                position: [vehicle.targetLongitude, vehicle.targetLatitude],
                content: `<div style="background: #52c41a; color: white; padding: 3px 6px; border-radius: 4px; font-size: 10px; white-space: nowrap; box-shadow: 0 2px 4px rgba(0,0,0,0.2);">
                    🎯 ${vehicle.targetStationName || '目标'}
                </div>`,
                offset: new AMap.Pixel(-10, -10),
                zIndex: 99
            });
            window.map.add(vehicle.destMarker);
        }
        vehicle.routeVisible = true;
    }
}

// 计算路线总长度（米）
function calculateOverviewPathLength(path) {
    let totalLength = 0;
    for (let i = 0; i < path.length - 1; i++) {
        const [lng1, lat1] = path[i];
        const [lng2, lat2] = path[i + 1];
        totalLength += calculateDistance(lat1, lng1, lat2, lng2);
    }
    return totalLength;
}

function startOverviewDispatchAnimationLoop() {
    const intervalMs = 50;
    const baseSpeedKmh = 60; // 基础速度 60 km/h
    const speedVariation = 10; // 随机扰动 ±10 km/h
    
    // 为每个车辆初始化速度和路线长度
    Object.values(overviewDispatchVehicles).forEach(vehicle => {
        if (!vehicle.pathLength) {
            vehicle.pathLength = calculateOverviewPathLength(vehicle.path);
            vehicle.traveledDistance = 0;
            vehicle.currentSpeedKmh = baseSpeedKmh + (Math.random() - 0.5) * 2 * speedVariation;
        }
    });
    
    overviewDispatchAnimationTimer = setInterval(() => {
        const vehiclesArrived = [];
        
        Object.values(overviewDispatchVehicles).forEach(vehicle => {
            if (!vehicle.animating || !vehicle.path || vehicle.path.length === 0) return;
            
            // 每秒更新一次随机速度
            if (!vehicle.lastSpeedUpdate || Date.now() - vehicle.lastSpeedUpdate > 1000) {
                vehicle.currentSpeedKmh = baseSpeedKmh + (Math.random() - 0.5) * 2 * speedVariation;
                vehicle.lastSpeedUpdate = Date.now();
            }
            
            // 计算本帧前进的距离（米）
            const speedMps = vehicle.currentSpeedKmh * 1000 / 3600;
            vehicle.traveledDistance += speedMps * (intervalMs / 1000);
            
            // 计算进度百分比
            if (vehicle.pathLength > 0) {
                vehicle.progress = Math.min(100, (vehicle.traveledDistance / vehicle.pathLength) * 100);
            }
            
            if (vehicle.progress >= 100) {
                vehicle.progress = 100;
                vehicle.animating = false;
                vehiclesArrived.push(vehicle.vehicleId);
                return;
            }
            
            // 根据已走距离找到当前位置索引
            let accumulatedDist = 0;
            let currentIndex = 0;
            for (let i = 0; i < vehicle.path.length - 1; i++) {
                const [lng1, lat1] = vehicle.path[i];
                const [lng2, lat2] = vehicle.path[i + 1];
                const segLen = calculateDistance(lat1, lng1, lat2, lng2);
                if (accumulatedDist + segLen >= vehicle.traveledDistance) {
                    currentIndex = i;
                    break;
                }
                accumulatedDist += segLen;
                currentIndex = i + 1;
            }
            
            if (currentIndex < vehicle.path.length) {
                if (vehicle.marker) {
                    // 计算精确位置
                    if (currentIndex < vehicle.path.length - 1) {
                        const [lng1, lat1] = vehicle.path[currentIndex];
                        const [lng2, lat2] = vehicle.path[currentIndex + 1];
                        const segLen = calculateDistance(lat1, lng1, lat2, lng2);
                        const segProgress = segLen > 0 ? (vehicle.traveledDistance - accumulatedDist) / segLen : 0;
                        const clampedProgress = Math.max(0, Math.min(1, segProgress));
                        const actualLng = lng1 + (lng2 - lng1) * clampedProgress;
                        const actualLat = lat1 + (lat2 - lat1) * clampedProgress;
                        vehicle.marker.setPosition([actualLng, actualLat]);
                    } else {
                        const pos = vehicle.path[currentIndex];
                        vehicle.marker.setPosition([pos[0], pos[1]]);
                    }
                }
            }
        });
        
        vehiclesArrived.forEach(vehicleId => {
            const vehicle = overviewDispatchVehicles[vehicleId];
            if (vehicle) {
                if (vehicle.polyline) {
                    window.map.remove(vehicle.polyline);
                    vehicle.polyline = null;
                }
                if (vehicle.destMarker) {
                    window.map.remove(vehicle.destMarker);
                    vehicle.destMarker = null;
                }
                if (vehicle.originMarker) {
                    window.map.remove(vehicle.originMarker);
                    vehicle.originMarker = null;
                }
                
                if (vehicle.marker && vehicle.targetLongitude && vehicle.targetLatitude) {
                    vehicle.marker.setPosition([vehicle.targetLongitude, vehicle.targetLatitude]);
                    vehicle.marker.setContent(`<div style="width: 20px; height: 20px; background: #52c41a; border-radius: 50%; box-shadow: 0 2px 6px rgba(0,0,0,0.3); border: 2px solid white; display: flex; align-items: center; justify-content: center;">
                        <span style="font-size: 8px;">🚗</span>
                    </div>`);
                    overviewMarkers.push(vehicle.marker);
                }
                
                delete overviewDispatchVehicles[vehicleId];
            }
            API.notifyDispatchArrived(vehicleId);
            loadStats();
            loadVehicles();
        });
        
        if (Object.keys(overviewDispatchVehicles).length === 0) {
            clearInterval(overviewDispatchAnimationTimer);
            overviewDispatchAnimationTimer = null;
        }
    }, intervalMs);
}

// 导出全局函数和变量
window.selectedTypes = selectedTypes;
window.overviewMarkers = overviewMarkers;
window.overviewDispatchVehicles = overviewDispatchVehicles;
window.clearOverviewMarkers = clearOverviewMarkers;
window.highlightCard = highlightCard;
window.refreshOverviewMap = refreshOverviewMap;
window.showParkingLotsOnMap = showParkingLotsOnMap;
window.showVehiclesOnMap = showVehiclesOnMap;
window.showStationsOnMap = showStationsOnMap;
window.showOrdersOnMap = showOrdersOnMap;
window.getSelectedVehicleStatuses = getSelectedVehicleStatuses;
window.filterVehiclesByStatus = filterVehiclesByStatus;
window.selectAllVehicleStatus = selectAllVehicleStatus;
window.deselectAllVehicleStatus = deselectAllVehicleStatus;
window.showStationsOnMapByType = showStationsOnMapByType;
window.showOrdersOnMapByStatus = showOrdersOnMapByStatus;
window.hideAllFilters = hideAllFilters;
window.startOverviewDispatchAnimation = startOverviewDispatchAnimation;
