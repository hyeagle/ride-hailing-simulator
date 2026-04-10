/**
 * 路线规划模块
 */

let routeMap = null;
let currentPath = [];
let polyline = null;
let clickMarker = null;
let infoWindow = null;
let pathPointMarkers = []; // 存储路径点标记

// 初始化路线规划地图
function initRouteMap() {
    if (!routeMap) {
        const mapContainer = document.getElementById('route-map');
        if (!mapContainer) {
            console.error('找不到地图容器 route-map');
            return;
        }
        
        routeMap = new AMap.Map('route-map', { 
            zoom: 11, 
            center: [118.795, 32.05], 
            viewMode: '2D' 
        });
        window.routeMap = routeMap;
        
        console.log('路线规划地图初始化成功');
    }
}

// 确保地图已初始化（延迟初始化）
function ensureRouteMapInitialized() {
    if (!routeMap) {
        initRouteMap();
    }
}

// 规划路线
async function planRoute() {
    // 确保地图已初始化
    ensureRouteMapInitialized();
    
    const origin = document.getElementById('route-origin').value.trim();
    const destination = document.getElementById('route-destination').value.trim();
    
    if (!origin || !destination) {
        alert('请输入起始地点和目的地点');
        return;
    }
    
    // 显示加载提示
    const routeInfo = document.getElementById('route-info');
    routeInfo.style.display = 'block';
    document.getElementById('route-distance').textContent = '规划中...';
    document.getElementById('route-duration').textContent = '规划中...';
    
    try {
        const result = await API.planRoute(origin, destination);
        
        if (result.status === 200 && result.path) {
            currentPath = result.path;
            
            // 更新路线信息
            document.getElementById('route-distance').textContent = (result.distance / 1000).toFixed(2) + ' 公里';
            document.getElementById('route-duration').textContent = Math.ceil(result.duration / 60) + ' 分钟';
            
            // 清除之前的路线和标记
            if (polyline) routeMap.remove(polyline);
            if (clickMarker) routeMap.remove(clickMarker);
            if (infoWindow) routeMap.clearInfoWindow();
            clearPathPointMarkers();
            
            // 绘制路线
            polyline = new AMap.Polyline({
                path: currentPath,
                strokeColor: '#722ed1',
                strokeWeight: 6,
                strokeOpacity: 0.8,
                lineJoin: 'round',
                lineCap: 'round'
            });
            routeMap.add(polyline);
            
            // 添加起点和终点标记
            addRouteMarkers(currentPath[0], currentPath[currentPath.length - 1]);
            
            // 渲染路径点
            renderPathPoints(currentPath);
            
            // 调整视野以包含整条路线
            routeMap.setFitView([polyline], false, [50, 50, 50, 50]);
            
        } else {
            alert('路线规划失败: ' + (result.message || '未知错误'));
            routeInfo.style.display = 'none';
        }
    } catch (error) {
        console.error('路线规划失败:', error);
        alert('路线规划失败，请检查输入的地址或坐标是否正确');
        routeInfo.style.display = 'none';
    }
}

// 添加起点和终点标记
function addRouteMarkers(start, end) {
    // 移除旧标记
    routeMap.getAllOverlays('marker').forEach(marker => {
        const content = marker.getContent();
        if (content && (content.includes('起点') || content.includes('终点'))) {
            routeMap.remove(marker);
        }
    });
    
    // 起点标记
    const startMarker = new AMap.Marker({
        position: start,
        content: `<div style="background: #52c41a; color: white; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: bold; white-space: nowrap; box-shadow: 0 2px 6px rgba(0,0,0,0.3);">
            📍 起点
        </div>`,
        offset: new AMap.Pixel(-20, -15),
        zIndex: 100
    });
    routeMap.add(startMarker);
    
    // 终点标记
    const endMarker = new AMap.Marker({
        position: end,
        content: `<div style="background: #f5222d; color: white; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: bold; white-space: nowrap; box-shadow: 0 2px 6px rgba(0,0,0,0.3);">
            🎯 终点
        </div>`,
        offset: new AMap.Pixel(-20, -15),
        zIndex: 100
    });
    routeMap.add(endMarker);
}

// 渲染路径点（显示所有路径点，可点击查看经纬度）
function renderPathPoints(path) {
    // 清除之前的路径点标记
    clearPathPointMarkers();
    
    // 为每个路径点创建标记
    path.forEach((point, index) => {
        const lng = Array.isArray(point) ? point[0] : point.longitude || point.lng;
        const lat = Array.isArray(point) ? point[1] : point.latitude || point.lat;
        
        // 创建路径点标记（小圆点）
        const marker = new AMap.Marker({
            position: [lng, lat],
            content: `<div style="width: 8px; height: 8px; background: #722ed1; border-radius: 50%; border: 2px solid white; box-shadow: 0 1px 3px rgba(0,0,0,0.3); cursor: pointer;"></div>`,
            offset: new AMap.Pixel(-6, -6),
            zIndex: 90
        });
        
        // 添加点击事件
        marker.on('click', function() {
            showCoordinateInfo(lng, lat, index);
        });
        
        routeMap.add(marker);
        pathPointMarkers.push(marker);
    });
    
    console.log(`已渲染 ${pathPointMarkers.length} 个路径点`);
}

// 清除路径点标记
function clearPathPointMarkers() {
    pathPointMarkers.forEach(marker => {
        routeMap.remove(marker);
    });
    pathPointMarkers = [];
}

// 显示坐标信息
function showCoordinateInfo(lng, lat, index) {
    // 移除之前的点击标记
    if (clickMarker) routeMap.remove(clickMarker);
    
    // 创建信息窗体
    const indexInfo = index !== undefined ? `<div style="margin-bottom: 5px;"><strong>路径点：</strong>第 ${index + 1} 个点</div>` : '';
    const infoContent = `
        <div style="padding: 10px; min-width: 200px;">
            <div style="font-weight: bold; margin-bottom: 8px; color: #722ed1;">📍 位置信息</div>
            ${indexInfo}
            <div style="margin-bottom: 5px;"><strong>经度：</strong>${lng.toFixed(6)}</div>
            <div style="margin-bottom: 5px;"><strong>纬度：</strong>${lat.toFixed(6)}</div>
            <div style="margin-top: 8px; padding-top: 8px; border-top: 1px solid #eee; font-size: 12px; color: #999;">
                经纬度格式：${lng.toFixed(6)},${lat.toFixed(6)}
            </div>
        </div>
    `;
    
    // 创建标记
    clickMarker = new AMap.Marker({
        position: [lng, lat],
        content: `<div style="width: 12px; height: 12px; background: #ff4d4f; border-radius: 50%; border: 2px solid white; box-shadow: 0 2px 6px rgba(0,0,0,0.4);"></div>`,
        offset: new AMap.Pixel(-6, -6),
        zIndex: 110
    });
    routeMap.add(clickMarker);
    
    // 显示信息窗体
    if (infoWindow) {
        infoWindow.setContent(infoContent);
        infoWindow.open(routeMap, [lng, lat]);
    } else {
        infoWindow = new AMap.InfoWindow({
            content: infoContent,
            offset: new AMap.Pixel(0, -20)
        });
        infoWindow.open(routeMap, [lng, lat]);
    }
}

// 清除路线
function clearRoute() {
    // 清除路线
    if (polyline) {
        routeMap.remove(polyline);
        polyline = null;
    }
    
    // 清除点击标记
    if (clickMarker) {
        routeMap.remove(clickMarker);
        clickMarker = null;
    }
    
    // 清除路径点标记
    clearPathPointMarkers();
    
    // 关闭信息窗体
    if (infoWindow) {
        routeMap.clearInfoWindow();
    }
    
    // 清除起点终点标记
    routeMap.getAllOverlays('marker').forEach(marker => {
        const content = marker.getContent();
        if (content && (content.includes('起点') || content.includes('终点'))) {
            routeMap.remove(marker);
        }
    });
    
    // 清空输入框
    document.getElementById('route-origin').value = '';
    document.getElementById('route-destination').value = '';
    
    // 隐藏路线信息
    document.getElementById('route-info').style.display = 'none';
    
    // 清空路径数据
    currentPath = [];
}

// 导出全局函数
window.initRouteMap = initRouteMap;
window.ensureRouteMapInitialized = ensureRouteMapInitialized;
window.planRoute = planRoute;
window.clearRoute = clearRoute;
window.clearPathPointMarkers = clearPathPointMarkers;
window.showCoordinateInfo = showCoordinateInfo;
