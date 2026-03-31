/**
 * 路线规划模块
 */

let currentPath = [];
let carMarker = null;
let polyline = null;
let isDriving = false;
let animationId = null;

// 规划路线
async function planRoute() {
    const start = document.getElementById('route-start').value.trim();
    const end = document.getElementById('route-end').value.trim();
    if (!start || !end) {
        alert('请输入起始地点和目的地点');
        return;
    }
    
    const result = await API.planRoute(start, end);
    
    if (result.status === 200) {
        currentPath = result.path;
        document.getElementById('route-info').style.display = 'block';
        document.getElementById('route-distance').textContent = (result.distance / 1000).toFixed(2) + ' 公里';
        document.getElementById('route-duration').textContent = Math.ceil(result.duration / 60) + ' 分钟';
        
        if (polyline) window.routeMap.remove(polyline);
        if (carMarker) window.routeMap.remove(carMarker);
        
        polyline = new AMap.Polyline({
            path: currentPath,
            strokeColor: '#1890ff',
            strokeWeight: 5
        });
        window.routeMap.add(polyline);
        window.routeMap.setFitView([polyline]);
        
        carMarker = new AMap.Marker({
            position: currentPath[0],
            content: '<div style="font-size: 24px;">🚗</div>',
            autoRotation: true
        });
        window.routeMap.add(carMarker);
        document.getElementById('driveBtn').disabled = false;
    } else {
        alert('路线规划失败: ' + result.message);
    }
}

// 开始/停止行驶
function startDriving() {
    if (isDriving) {
        stopDriving();
        return;
    }
    
    isDriving = true;
    document.getElementById('driveBtn').textContent = '停止行驶';
    
    let currentIndex = 0;
    let traveledDistance = 0;
    let currentSpeedKms = 40;
    const intervalMs = 30;
    
    const speedInterval = setInterval(() => {
        if (!isDriving) {
            clearInterval(speedInterval);
            return;
        }
        currentSpeedKms = 40 + (Math.random() - 0.5) * 10;
        document.getElementById('current-speed').textContent = currentSpeedKms.toFixed(1) + ' km/s';
    }, 1000);
    
    animationId = setInterval(() => {
        if (!isDriving) {
            clearInterval(speedInterval);
            clearInterval(animationId);
            return;
        }
        
        const speedMps = currentSpeedKms * 1000;
        traveledDistance += speedMps * (intervalMs / 1000);
        
        while (currentIndex < currentPath.length - 1) {
            const [lng1, lat1] = currentPath[currentIndex];
            const [lng2, lat2] = currentPath[currentIndex + 1];
            const segDist = calculateDistance(lat1, lng1, lat2, lng2);
            if (traveledDistance >= segDist) {
                traveledDistance -= segDist;
                currentIndex++;
            } else {
                break;
            }
        }
        
        if (currentIndex >= currentPath.length - 1) {
            carMarker.setPosition(currentPath[currentPath.length - 1]);
            stopDriving();
            alert('已到达目的地！');
            return;
        }
        
        const [lng1, lat1] = currentPath[currentIndex];
        const [lng2, lat2] = currentPath[currentIndex + 1];
        const segDist = calculateDistance(lat1, lng1, lat2, lng2);
        const progress = segDist > 0 ? traveledDistance / segDist : 1;
        
        carMarker.setPosition([lng1 + (lng2 - lng1) * progress, lat1 + (lat2 - lat1) * progress]);
        carMarker.setAngle(Math.atan2(lat2 - lat1, lng2 - lng1) * 180 / Math.PI);
    }, intervalMs);
}

// 停止行驶
function stopDriving() {
    isDriving = false;
    document.getElementById('driveBtn').textContent = '开始行驶';
    document.getElementById('current-speed').textContent = '-';
}

// 导出全局函数
window.planRoute = planRoute;
window.startDriving = startDriving;
window.stopDriving = stopDriving;
