/**
 * 派单和调度模块
 */

// ===== 订单派单相关 =====

let orderDispatchVehicles = {};
let orderDispatchAnimationTimer = null;

// 启动订单派单更新
function startOrderDispatchUpdate() {
    loadOrderDispatchInfo();
    orderDispatchUpdateInterval = setInterval(() => {
        loadOrderDispatchInfo();
    }, 5000);
}

// 加载订单派单信息
async function loadOrderDispatchInfo() {
    try {
        const data = await API.getOrderDispatchInfo();
        updateOrderDispatchTable(data);
        syncOrderDispatchVehicles(data);
    } catch (error) {
        console.error('加载派单信息失败', error);
    }
}

// 更新派单表格
function updateOrderDispatchTable(data) {
    const tbody = document.querySelector('#order-dispatch-table tbody');
    tbody.innerHTML = data.map(d => {
        const progress = d.pickupProgress !== undefined ? d.pickupProgress : (d.deliveryProgress || 0);
        const routeType = d.status === '接乘中' ? '前往接乘点' : '履约中';
        const progressColor = routeType === '前往接乘点' ? '#fa8c16' : '#52c41a';
        
        return `
        <tr>
            <td>${d.vehicleId}</td>
            <td>${d.vin}</td>
            <td>${d.orderId || '-'}</td>
            <td><span class="badge ${getBadgeClass(d.status)}">${d.status}</span></td>
            <td>${routeType}</td>
            <td>
                <div style="background: #f0f0f0; border-radius: 4px; height: 20px; overflow: hidden;">
                    <div style="background: ${progressColor}; width: ${progress}%; height: 100%; transition: width 0.1s;"></div>
                </div>
                <small>${progress}%</small>
            </td>
            <td>${d.originStationName || d.originStationId || '-'}</td>
            <td>${d.destStationName || d.destStationId || '-'}</td>
        </tr>
    `}).join('');
}

// 同步派单车辆状态
function syncOrderDispatchVehicles(data) {
    const currentVehicleIds = new Set(Object.keys(orderDispatchVehicles));
    const newVehicleIds = new Set(data.map(d => d.vehicleId));
    
    // 移除已完成的车辆
    currentVehicleIds.forEach(id => {
        if (!newVehicleIds.has(id)) {
            const vehicle = orderDispatchVehicles[id];
            if (vehicle.marker) window.orderDispatchMap.remove(vehicle.marker);
            if (vehicle.polyline) window.orderDispatchMap.remove(vehicle.polyline);
            if (vehicle.destMarker) window.orderDispatchMap.remove(vehicle.destMarker);
            if (vehicle.originMarker) window.orderDispatchMap.remove(vehicle.originMarker);
            delete orderDispatchVehicles[id];
        }
    });
    
    // 添加或更新派单车辆
    data.forEach(d => {
        const path = d.pickupPath || d.deliveryPath;
        
        if (!orderDispatchVehicles[d.vehicleId] && path && path.length > 0) {
            const markerColor = d.status === '接乘中' ? '#fa8c16' : '#52c41a';
            const marker = new AMap.Marker({
                position: [path[0][0], path[0][1]],
                content: `<div style="background: ${markerColor}; color: white; padding: 6px 10px; border-radius: 4px; font-size: 13px; white-space: nowrap; box-shadow: 0 2px 8px rgba(0,0,0,0.3);">
                    🚗 ${d.vehicleId} <span style="font-size: 11px;">(${d.status})</span>
                </div>`,
                offset: new AMap.Pixel(-15, -15),
                autoRotation: true
            });
            window.orderDispatchMap.add(marker);
            
            const polyline = new AMap.Polyline({
                path: path,
                strokeColor: markerColor,
                strokeWeight: 5,
                strokeOpacity: 0.8
            });
            window.orderDispatchMap.add(polyline);
            
            let originMarker = null;
            if (d.originLongitude && d.originLatitude) {
                originMarker = new AMap.Marker({
                    position: [d.originLongitude, d.originLatitude],
                    content: `<div style="background: #1890ff; color: white; padding: 6px 10px; border-radius: 4px; font-size: 13px; white-space: nowrap; box-shadow: 0 2px 8px rgba(0,0,0,0.3);">
                        📍 ${d.originStationName || '起点'}
                    </div>`,
                    offset: new AMap.Pixel(-15, -15)
                });
                window.orderDispatchMap.add(originMarker);
            }
            
            let destMarker = null;
            if (d.destLongitude && d.destLatitude) {
                destMarker = new AMap.Marker({
                    position: [d.destLongitude, d.destLatitude],
                    content: `<div style="background: #52c41a; color: white; padding: 6px 10px; border-radius: 4px; font-size: 13px; white-space: nowrap; box-shadow: 0 2px 8px rgba(0,0,0,0.3);">
                        🎯 ${d.destStationName || '终点'}
                    </div>`,
                    offset: new AMap.Pixel(-15, -15)
                });
                window.orderDispatchMap.add(destMarker);
            }
            
            orderDispatchVehicles[d.vehicleId] = {
                marker, polyline, originMarker, destMarker,
                path: path,
                currentIndex: 0,
                progress: d.pickupProgress !== undefined ? d.pickupProgress : (d.deliveryProgress || 0),
                status: d.status,
                animating: true,
                vehicleId: d.vehicleId
            };
            
            window.orderDispatchMap.setFitView([polyline], false, [50, 50, 50, 50]);
        } else if (orderDispatchVehicles[d.vehicleId]) {
            const vehicle = orderDispatchVehicles[d.vehicleId];
            vehicle.progress = d.pickupProgress !== undefined ? d.pickupProgress : (d.deliveryProgress || 0);
            vehicle.status = d.status;
        }
    });
    
    if (!orderDispatchAnimationTimer && Object.keys(orderDispatchVehicles).length > 0) {
        startOrderDispatchAnimation();
    }
}

// 启动订单派单动画
function startOrderDispatchAnimation() {
    const intervalMs = 50;
    const progressPerSecond = 2;
    
    orderDispatchAnimationTimer = setInterval(() => {
        const vehiclesToRemove = [];
        
        Object.values(orderDispatchVehicles).forEach(vehicle => {
            if (!vehicle.animating || !vehicle.path || vehicle.path.length === 0) return;
            
            vehicle.progress += progressPerSecond * (intervalMs / 1000);
            
            if (vehicle.progress >= 100) {
                vehicle.progress = 100;
                vehicle.animating = false;
                vehiclesToRemove.push(vehicle.vehicleId);
                return;
            }
            
            const totalPoints = vehicle.path.length;
            const currentIndex = Math.floor((vehicle.progress / 100) * (totalPoints - 1));
            
            if (currentIndex < totalPoints) {
                const pos = vehicle.path[currentIndex];
                if (vehicle.marker && pos.length >= 2) {
                    vehicle.marker.setPosition([pos[0], pos[1]]);
                    
                    if (currentIndex < totalPoints - 1) {
                        const nextPos = vehicle.path[currentIndex + 1];
                        const angle = Math.atan2(nextPos[1] - pos[1], nextPos[0] - pos[0]) * 180 / Math.PI;
                        vehicle.marker.setAngle(angle);
                    }
                }
            }
            
            if (vehicle.marker) {
                const markerColor = vehicle.status === '接乘中' ? '#fa8c16' : '#52c41a';
                vehicle.marker.setContent(`<div style="background: ${markerColor}; color: white; padding: 6px 10px; border-radius: 4px; font-size: 13px; white-space: nowrap; box-shadow: 0 2px 8px rgba(0,0,0,0.3);">
                    🚗 ${vehicle.vehicleId} <span style="font-size: 11px;">(${Math.round(vehicle.progress)}%)</span>
                </div>`);
            }
        });
        
        vehiclesToRemove.forEach(vehicleId => {
            const vehicle = orderDispatchVehicles[vehicleId];
            if (vehicle.status === '接乘中') {
                notifyVehicleArrivedPickup(vehicleId);
            } else if (vehicle.status === '履约中') {
                notifyVehicleArrivedDestination(vehicleId);
            }
        });
        
    }, intervalMs);
}

// 通知后端车辆到达接乘点
async function notifyVehicleArrivedPickup(vehicleId) {
    try {
        await API.notifyArrivedPickup(vehicleId);
        console.log(`车辆 ${vehicleId} 已到达接乘点`);
        
        const vehicle = orderDispatchVehicles[vehicleId];
        if (vehicle) {
            if (vehicle.marker) window.orderDispatchMap.remove(vehicle.marker);
            if (vehicle.polyline) window.orderDispatchMap.remove(vehicle.polyline);
            delete orderDispatchVehicles[vehicleId];
        }
        
        loadStats();
    } catch (error) {
        console.error(`通知车辆到达接乘点失败: ${vehicleId}`, error);
    }
}

// 通知后端车辆到达目的地
async function notifyVehicleArrivedDestination(vehicleId) {
    try {
        await API.notifyArrivedDestination(vehicleId);
        console.log(`车辆 ${vehicleId} 已到达目的地`);
        
        const vehicle = orderDispatchVehicles[vehicleId];
        if (vehicle) {
            if (vehicle.marker) window.orderDispatchMap.remove(vehicle.marker);
            if (vehicle.polyline) window.orderDispatchMap.remove(vehicle.polyline);
            if (vehicle.originMarker) window.orderDispatchMap.remove(vehicle.originMarker);
            if (vehicle.destMarker) window.orderDispatchMap.remove(vehicle.destMarker);
            delete orderDispatchVehicles[vehicleId];
        }
        
        loadStats();
    } catch (error) {
        console.error(`通知车辆到达目的地失败: ${vehicleId}`, error);
    }
}

// 显示派单车辆在地图上
async function showOrderDispatchOnMap() {
    hideAllFilters();
    clearOverviewMarkers();
    highlightCard('dispatch');
    
    const data = await API.getOrderDispatchInfo();
    
    data.forEach(d => {
        const path = d.pickupPath || d.deliveryPath;
        if (path && path.length > 0) {
            const markerColor = d.status === '接乘中' ? '#fa8c16' : '#52c41a';
            const marker = new AMap.Marker({
                position: [d.longitude, d.latitude],
                content: `<div style="background: ${markerColor}; color: white; padding: 4px 8px; border-radius: 4px; font-size: 12px; white-space: nowrap; box-shadow: 0 2px 6px rgba(0,0,0,0.3);">
                    🚗 ${d.vehicleId} (${d.status})
                </div>`,
                offset: new AMap.Pixel(-15, -15)
            });
            window.map.add(marker);
            window.overviewMarkers.push(marker);
            
            const polyline = new AMap.Polyline({
                path: path,
                strokeColor: markerColor,
                strokeWeight: 4,
                strokeOpacity: 0.8
            });
            window.map.add(polyline);
            window.overviewMarkers.push(polyline);
        }
    });
}

// 触发派单
async function triggerOrderDispatch() {
    try {
        const msg = await API.triggerOrderDispatch();
        alert(msg);
        loadOrderDispatchInfo();
        loadStats();
    } catch (error) {
        alert('触发派单失败：' + error.message);
    }
}

// ===== 车辆调度相关 =====

let vehicleDispatchVehicles = {};
let vehicleDispatchAnimationTimer = null;

// 启动车辆调度更新
function startVehicleDispatchUpdate() {
    loadVehicleDispatchInfo();
    vehicleDispatchUpdateInterval = setInterval(() => {
        loadVehicleDispatchInfo();
    }, 5000);
}

// 加载车辆调度信息
async function loadVehicleDispatchInfo() {
    try {
        const data = await API.getDispatchInfo();
        updateVehicleDispatchTable(data);
        syncVehicleDispatchVehicles(data);
    } catch (error) {
        console.error('加载车辆调度信息失败', error);
    }
}

// 更新调度表格
function updateVehicleDispatchTable(data) {
    const tbody = document.querySelector('#vehicle-dispatch-table tbody');
    tbody.innerHTML = data.map(d => {
        const vehicle = vehicleDispatchVehicles[d.vehicleId];
        const displayProgress = vehicle ? Math.round(vehicle.progress) : (d.progress || 0);
        return `
        <tr>
            <td>${d.vehicleId}</td>
            <td>${d.vin}</td>
            <td>${d.targetStationName || d.targetStationId}</td>
            <td>
                <div style="background: #f0f0f0; border-radius: 4px; height: 20px; overflow: hidden;">
                    <div style="background: linear-gradient(90deg, #52c41a, #1890ff); width: ${displayProgress}%; height: 100%; transition: width 0.1s;"></div>
                </div>
                <small>${displayProgress}%</small>
            </td>
            <td>${d.startTime ? new Date(d.startTime).toLocaleString('zh-CN') : '-'}</td>
            <td><span class="badge badge-warning">调度中</span></td>
        </tr>
    `}).join('');
}

// 同步车辆调度状态
function syncVehicleDispatchVehicles(data) {
    const currentVehicleIds = new Set(Object.keys(vehicleDispatchVehicles));
    const newVehicleIds = new Set(data.map(d => d.vehicleId));
    
    // 移除已完成的车辆
    currentVehicleIds.forEach(id => {
        if (!newVehicleIds.has(id)) {
            delete vehicleDispatchVehicles[id];
        }
    });
    
    data.forEach(d => {
        if (!vehicleDispatchVehicles[d.vehicleId] && d.path && d.path.length > 0) {
            vehicleDispatchVehicles[d.vehicleId] = {
                path: d.path,
                currentIndex: 0,
                progress: d.progress || 0,
                animating: true,
                vehicleId: d.vehicleId
            };
        }
    });
    
    if (!vehicleDispatchAnimationTimer && Object.keys(vehicleDispatchVehicles).length > 0) {
        startVehicleDispatchAnimation();
    }
}

// 计算路线总长度（米）
function calculatePathLength(path) {
    let totalLength = 0;
    for (let i = 0; i < path.length - 1; i++) {
        const [lng1, lat1] = path[i];
        const [lng2, lat2] = path[i + 1];
        totalLength += calculateDistance(lat1, lng1, lat2, lng2);
    }
    return totalLength;
}

// 启动车辆调度动画
function startVehicleDispatchAnimation() {
    const intervalMs = 50;
    const speedVariation = 10; // 随机扰动 ±10 km/h
    
    // 获取页面配置的速度
    function getBaseSpeedKmh() {
        const speedInput = document.getElementById('vehicle-speed-config');
        if (speedInput) {
            const speed = parseInt(speedInput.value) || 100;
            return Math.max(10, Math.min(200, speed));
        }
        return 100; // 默认 100 km/h
    }
    
    // 为每个车辆初始化速度和路线长度
    Object.values(vehicleDispatchVehicles).forEach(vehicle => {
        if (!vehicle.pathLength) {
            vehicle.pathLength = calculatePathLength(vehicle.path);
            vehicle.traveledDistance = 0;
            vehicle.currentSpeedKmh = getBaseSpeedKmh() + (Math.random() - 0.5) * 2 * speedVariation;
        }
    });
    
    vehicleDispatchAnimationTimer = setInterval(() => {
        const vehiclesToRemove = [];
        
        Object.values(vehicleDispatchVehicles).forEach(vehicle => {
            if (!vehicle.animating || !vehicle.path || vehicle.path.length === 0) return;
            
            // 每秒更新一次随机速度
            if (!vehicle.lastSpeedUpdate || Date.now() - vehicle.lastSpeedUpdate > 1000) {
                vehicle.currentSpeedKmh = getBaseSpeedKmh() + (Math.random() - 0.5) * 2 * speedVariation;
                vehicle.lastSpeedUpdate = Date.now();
            }
            
            // 计算本帧前进的距离（米）
            const speedMps = vehicle.currentSpeedKmh * 1000 / 3600; // km/h 转 m/s
            vehicle.traveledDistance += speedMps * (intervalMs / 1000);
            
            // 计算进度百分比
            if (vehicle.pathLength > 0) {
                vehicle.progress = Math.min(100, (vehicle.traveledDistance / vehicle.pathLength) * 100);
            }
            
            if (vehicle.progress >= 100) {
                vehicle.progress = 100;
                vehicle.animating = false;
                vehiclesToRemove.push(vehicle.vehicleId);
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
            
            // 根据已走距离找到当前位置索引
        });
        
        vehiclesToRemove.forEach(vehicleId => {
            notifyVehicleDispatchArrived(vehicleId);
        });
        
    }, intervalMs);
}

// 通知后端车辆调度已到达
async function notifyVehicleDispatchArrived(vehicleId) {
    try {
        await API.notifyDispatchArrived(vehicleId);
        console.log(`车辆 ${vehicleId} 已到达调度目标站点`);
        
        delete vehicleDispatchVehicles[vehicleId];
        
        loadStats();
        loadVehicles();
    } catch (error) {
        console.error(`通知车辆调度到达失败: ${vehicleId}`, error);
    }
}

// 触发车辆调度
async function triggerVehicleDispatch() {
    try {
        const msg = await API.triggerDispatch();
        alert(msg);
        loadVehicleDispatchInfo();
        loadStats();
    } catch (error) {
        alert('触发调度失败：' + error.message);
    }
}

// 导出全局函数
window.startOrderDispatchUpdate = startOrderDispatchUpdate;
window.loadOrderDispatchInfo = loadOrderDispatchInfo;
window.showOrderDispatchOnMap = showOrderDispatchOnMap;
window.triggerOrderDispatch = triggerOrderDispatch;
window.startVehicleDispatchUpdate = startVehicleDispatchUpdate;
window.loadVehicleDispatchInfo = loadVehicleDispatchInfo;
window.triggerVehicleDispatch = triggerVehicleDispatch;
