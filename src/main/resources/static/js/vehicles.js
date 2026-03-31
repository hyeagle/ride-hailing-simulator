/**
 * 车辆管理模块
 */

// 加载车辆列表
async function loadVehicles() {
    const data = await API.getVehicles();
    renderVehicles(data);
    loadVehicleGridOptions(data);
}

// 加载网格选项
function loadVehicleGridOptions(vehicles) {
    const grids = [...new Set(vehicles.map(v => v.gisGrid))].filter(g => g).sort();
    const select = document.getElementById('vehicle-grid-filter');
    const currentValue = select.value;
    select.innerHTML = '<option value="">全部网格</option>' + grids.map(g => `<option value="${g}">${g}</option>`).join('');
    if (grids.includes(currentValue)) {
        select.value = currentValue;
    }
}

// 渲染车辆表格
function renderVehicles(data) {
    const tbody = document.querySelector('#vehicle-table tbody');
    tbody.innerHTML = data.map(v => `
        <tr>
            <td>${v.id}</td><td>${v.vin}</td>
            <td><span class="badge ${getBadgeClass(v.status)}">${v.status}</span></td>
            <td>${v.orderId || '-'}</td><td>${v.batteryPercent}%</td><td>${v.gisGrid || '-'}</td>
            <td>${v.longitude.toFixed(6)}</td><td>${v.latitude.toFixed(6)}</td>
            <td>
                <button class="btn btn-warning" onclick="editVehicle('${v.id}')">编辑</button>
                <button class="btn btn-danger" onclick="deleteVehicle('${v.id}')">删除</button>
            </td>
        </tr>
    `).join('');
}

// 切换筛选面板
function toggleVehicleFilters() {
    const panel = document.getElementById('vehicle-filter-panel');
    panel.style.display = panel.style.display === 'none' ? 'block' : 'none';
}

// 重置筛选条件
function resetVehicleFilters() {
    document.getElementById('vehicle-vin-filter').value = '';
    document.getElementById('vehicle-grid-filter').value = '';
    document.getElementById('vehicle-battery-min').value = '';
    document.getElementById('vehicle-battery-max').value = '';
    document.getElementById('vehicle-status-filter').value = '';
    loadVehicles();
}

// 综合查询车辆
async function searchVehicles() {
    const vin = document.getElementById('vehicle-vin-filter').value.trim();
    const gisGrid = document.getElementById('vehicle-grid-filter').value;
    const minBattery = document.getElementById('vehicle-battery-min').value;
    const maxBattery = document.getElementById('vehicle-battery-max').value;
    const status = document.getElementById('vehicle-status-filter').value;
    
    let data;
    if (vin || gisGrid || minBattery || maxBattery) {
        const params = new URLSearchParams();
        if (vin) params.append('vin', vin);
        if (gisGrid) params.append('gisGrid', gisGrid);
        if (minBattery) params.append('minBattery', minBattery);
        if (maxBattery) params.append('maxBattery', maxBattery);
        data = await API.searchVehicles(params);
    } else {
        data = await API.getVehicles();
    }
    
    if (status) {
        data = data.filter(v => v.status === status);
    }
    
    renderVehicles(data);
}

// 编辑车辆
async function editVehicle(id) {
    const data = await API.getVehicle(id);
    showModal('vehicle');
    document.getElementById('modal-title').textContent = '编辑车辆';
    document.getElementById('vehicle-vin').value = data.vin;
    document.getElementById('vehicle-status').value = data.status;
    document.getElementById('vehicle-battery').value = data.batteryPercent;
    document.getElementById('vehicle-lng').value = data.longitude;
    document.getElementById('vehicle-lat').value = data.latitude;
    document.querySelector('#modal-body .btn-primary').onclick = () => saveVehicle(id);
}

// 保存车辆
async function saveVehicle(id) {
    const body = {
        vin: document.getElementById('vehicle-vin').value,
        status: document.getElementById('vehicle-status').value,
        batteryPercent: parseInt(document.getElementById('vehicle-battery').value),
        longitude: parseFloat(document.getElementById('vehicle-lng').value),
        latitude: parseFloat(document.getElementById('vehicle-lat').value)
    };
    
    if (id) {
        await API.updateVehicle(id, body);
    } else {
        await API.createVehicle(body);
    }
    
    closeModal();
    loadVehicles();
}

// 删除车辆
async function deleteVehicle(id) {
    if (confirm('确定删除？')) {
        await API.deleteVehicle(id);
        loadVehicles();
    }
}

// 显示车辆上电弹窗
function showPowerOnModal() {
    document.getElementById('modal').classList.add('show');
    document.getElementById('modal-title').textContent = '⚡ 车辆上电';
    document.getElementById('modal-body').innerHTML = `
        <div class="form-group">
            <label>上电数量</label>
            <input type="number" id="power-on-count" value="1" min="1" max="100" style="width: 100%;">
        </div>
        <p style="color: #666; font-size: 12px; margin-bottom: 15px;">
            将随机选择指定数量的"休息中"车辆，状态改为"巡游中"
        </p>
        <button class="btn btn-success" onclick="powerOnVehicles()">确认上电</button>
    `;
}

// 执行车辆上电
async function powerOnVehicles() {
    const count = document.getElementById('power-on-count').value;
    if (!count || count < 1) {
        alert('请输入有效的上电数量');
        return;
    }
    
    try {
        const actualCount = await API.powerOnVehicles(count);
        closeModal();
        
        if (actualCount === 0) {
            alert('没有可上电的车辆（无"休息中"状态的车辆）');
        } else if (actualCount < count) {
            alert(`成功上电 ${actualCount} 辆车辆（实际休息中车辆不足）`);
        } else {
            alert(`成功上电 ${actualCount} 辆车辆`);
        }
        
        loadVehicles();
        loadStats();
    } catch (error) {
        alert('上电失败：' + error.message);
    }
}

// 导出全局函数
window.loadVehicles = loadVehicles;
window.renderVehicles = renderVehicles;
window.toggleVehicleFilters = toggleVehicleFilters;
window.resetVehicleFilters = resetVehicleFilters;
window.searchVehicles = searchVehicles;
window.editVehicle = editVehicle;
window.saveVehicle = saveVehicle;
window.deleteVehicle = deleteVehicle;
window.showPowerOnModal = showPowerOnModal;
window.powerOnVehicles = powerOnVehicles;
