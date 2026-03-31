/**
 * 停车场管理模块
 */

// 加载停车场列表
async function loadParkingLots() {
    const data = await API.getParkingLots();
    renderParkingLots(data);
}

// 渲染停车场表格
function renderParkingLots(data) {
    const tbody = document.querySelector('#parking-table tbody');
    tbody.innerHTML = data.map(p => `
        <tr>
            <td>${p.id}</td><td>${p.name}</td><td>${p.gisGrid}</td><td>${p.capacity}</td>
            <td>${p.longitude.toFixed(6)}</td><td>${p.latitude.toFixed(6)}</td>
            <td>
                <button class="btn btn-warning" onclick="editParkingLot('${p.id}')">编辑</button>
                <button class="btn btn-danger" onclick="deleteParkingLot('${p.id}')">删除</button>
            </td>
        </tr>
    `).join('');
}

// 编辑停车场
async function editParkingLot(id) {
    const data = await API.getParkingLot(id);
    showModal('parking');
    document.getElementById('modal-title').textContent = '编辑停车场';
    document.getElementById('parking-name').value = data.name;
    document.getElementById('parking-gis').value = data.gisGrid;
    document.getElementById('parking-capacity').value = data.capacity;
    document.getElementById('parking-lng').value = data.longitude;
    document.getElementById('parking-lat').value = data.latitude;
    document.querySelector('#modal-body .btn-primary').onclick = () => saveParkingLot(id);
}

// 保存停车场
async function saveParkingLot(id) {
    const body = {
        name: document.getElementById('parking-name').value,
        gisGrid: document.getElementById('parking-gis').value,
        capacity: parseInt(document.getElementById('parking-capacity').value),
        longitude: parseFloat(document.getElementById('parking-lng').value),
        latitude: parseFloat(document.getElementById('parking-lat').value)
    };
    
    if (id) {
        await API.updateParkingLot(id, body);
    } else {
        await API.createParkingLot(body);
    }
    
    closeModal();
    loadParkingLots();
}

// 删除停车场
async function deleteParkingLot(id) {
    if (confirm('确定删除？')) {
        await API.deleteParkingLot(id);
        loadParkingLots();
    }
}

// 导出全局函数
window.loadParkingLots = loadParkingLots;
window.renderParkingLots = renderParkingLots;
window.editParkingLot = editParkingLot;
window.saveParkingLot = saveParkingLot;
window.deleteParkingLot = deleteParkingLot;
