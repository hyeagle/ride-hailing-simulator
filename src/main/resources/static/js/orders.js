/**
 * 订单管理模块
 */

// 加载订单列表
async function loadOrders() {
    const data = await API.getOrders();
    renderOrders(data);
}

// 渲染订单表格
function renderOrders(data) {
    const tbody = document.querySelector('#order-table tbody');
    tbody.innerHTML = data.map(o => `
        <tr>
            <td>${o.id}</td><td>${o.originStationId}</td><td>${o.destinationStationId}</td>
            <td>${formatTime(o.startTime)}</td><td>${formatTime(o.estimatedArrivalTime)}</td>
            <td>¥${o.estimatedRevenue.toFixed(2)}</td>
            <td><span class="badge ${getBadgeClass(o.status)}">${o.status}</span></td>
            <td>
                ${o.status === '待派单' ? `<button class="btn btn-primary" onclick="showModal('assign', '${o.id}')">派单</button>` : ''}
                ${o.status === '进行中' ? `<button class="btn btn-success" onclick="completeOrder('${o.id}')">完成</button>` : ''}
                ${o.status !== '已完成' && o.status !== '已取消' ? `<button class="btn btn-danger" onclick="cancelOrder('${o.id}')">取消</button>` : ''}
            </td>
        </tr>
    `).join('');
}

// 筛选订单
async function filterOrders() {
    const status = document.getElementById('order-status-filter').value;
    if (!status) {
        loadOrders();
        return;
    }
    const data = await API.getOrdersByStatus(status);
    renderOrders(data);
}

// 保存订单
async function saveOrder() {
    const body = {
        originStationId: parseInt(document.getElementById('order-origin').value),
        destinationStationId: parseInt(document.getElementById('order-dest').value),
        estimatedRevenue: parseFloat(document.getElementById('order-revenue').value)
    };
    await API.createOrder(body);
    closeModal();
    loadOrders();
}

// 派单
async function assignOrder(orderId) {
    const vehicleId = document.getElementById('assign-vehicle').value;
    await API.assignOrder(orderId, vehicleId);
    closeModal();
    loadOrders();
    loadVehicles();
}

// 完成订单
async function completeOrder(id) {
    await API.completeOrder(id);
    loadOrders();
}

// 取消订单
async function cancelOrder(id) {
    if (confirm('确定取消？')) {
        await API.cancelOrder(id);
        loadOrders();
    }
}

// 显示模拟订单弹窗
function showSimulateModal() {
    document.getElementById('modal').classList.add('show');
    document.getElementById('modal-title').textContent = '📊 模拟订单';
    document.getElementById('modal-body').innerHTML = `
        <div class="form-group">
            <label>订单数量</label>
            <input type="number" id="simulate-count" value="10" min="1" max="1000" style="width: 100%;">
        </div>
        <p style="color: #666; font-size: 12px; margin-bottom: 15px;">
            将随机选择站点作为起止地点，批量创建模拟订单
        </p>
        <button class="btn btn-success" onclick="simulateOrders()">确认创建</button>
    `;
}

// 执行模拟订单
async function simulateOrders() {
    const count = document.getElementById('simulate-count').value;
    if (!count || count < 1) {
        alert('请输入有效的订单数量');
        return;
    }
    
    try {
        const created = await API.simulateOrders(count);
        closeModal();
        
        if (created === 0) {
            alert('订单创建失败');
        } else if (created < count) {
            alert(`成功创建并派单 ${created} 个订单（部分订单创建失败）`);
        } else {
            alert(`成功创建并派单 ${created} 个订单`);
        }
        
        loadOrders();
        loadStats();
        loadOrderDispatchInfo();
    } catch (error) {
        alert('创建订单失败：' + error.message);
    }
}

// 导出全局函数
window.loadOrders = loadOrders;
window.renderOrders = renderOrders;
window.filterOrders = filterOrders;
window.saveOrder = saveOrder;
window.assignOrder = assignOrder;
window.completeOrder = completeOrder;
window.cancelOrder = cancelOrder;
window.showSimulateModal = showSimulateModal;
window.simulateOrders = simulateOrders;
