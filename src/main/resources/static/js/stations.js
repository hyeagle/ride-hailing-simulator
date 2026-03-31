/**
 * 站点管理模块
 */

// 加载站点列表
async function loadStations() {
    const data = await API.getStations();
    renderStations(data.slice(0, 100));
}

// 渲染站点表格
function renderStations(data) {
    const tbody = document.querySelector('#station-table tbody');
    tbody.innerHTML = data.map(s => `
        <tr>
            <td>${s.id}</td><td>${s.name}</td>
            <td><span class="badge badge-info">${s.type}</span></td>
            <td>${s.longitude.toFixed(6)}</td><td>${s.latitude.toFixed(6)}</td>
            <td>${s.remark || '-'}</td>
            <td>
                <button class="btn btn-warning" onclick="editStation('${s.id}')">编辑</button>
                <button class="btn btn-danger" onclick="deleteStation('${s.id}')">删除</button>
            </td>
        </tr>
    `).join('');
}

// 筛选站点
async function filterStations() {
    const type = document.getElementById('station-type-filter').value;
    if (!type) {
        loadStations();
        return;
    }
    const data = await API.getStationsByType(type);
    renderStations(data);
}

// 编辑站点
async function editStation(id) {
    const data = await API.getStation(id);
    showModal('station');
    document.getElementById('modal-title').textContent = '编辑站点';
    document.getElementById('station-name').value = data.name;
    document.getElementById('station-type').value = data.type;
    document.getElementById('station-lng').value = data.longitude;
    document.getElementById('station-lat').value = data.latitude;
    document.getElementById('station-remark').value = data.remark || '';
    document.querySelector('#modal-body .btn-primary').onclick = () => saveStation(id);
}

// 保存站点
async function saveStation(id) {
    const body = {
        name: document.getElementById('station-name').value,
        type: document.getElementById('station-type').value,
        longitude: parseFloat(document.getElementById('station-lng').value),
        latitude: parseFloat(document.getElementById('station-lat').value),
        remark: document.getElementById('station-remark').value
    };
    
    if (id) {
        await API.updateStation(id, body);
    } else {
        await API.createStation(body);
    }
    
    closeModal();
    loadStations();
}

// 删除站点
async function deleteStation(id) {
    if (confirm('确定删除？')) {
        await API.deleteStation(id);
        loadStations();
    }
}

// 导出全局函数
window.loadStations = loadStations;
window.renderStations = renderStations;
window.filterStations = filterStations;
window.editStation = editStation;
window.saveStation = saveStation;
window.deleteStation = deleteStation;
