/**
 * 模态框模块
 */

// 显示模态框
function showModal(type, id) {
    document.getElementById('modal').classList.add('show');
    const title = {
        parking: '停车场',
        vehicle: '车辆',
        station: '站点',
        order: '订单',
        assign: '派单',
        simulate: '模拟订单'
    };
    document.getElementById('modal-title').textContent = (title[type] || type);
    
    if (type === 'simulate') {
        showSimulateModal();
        return;
    }
    
    if (type === 'parking') {
        document.getElementById('modal-body').innerHTML = `
            <div class="form-group"><label>名称</label><input type="text" id="parking-name"></div>
            <div class="form-group"><label>GIS网格</label><input type="text" id="parking-gis"></div>
            <div class="form-group"><label>容量</label><input type="number" id="parking-capacity"></div>
            <div class="form-group"><label>经度</label><input type="number" step="0.000001" id="parking-lng"></div>
            <div class="form-group"><label>纬度</label><input type="number" step="0.000001" id="parking-lat"></div>
            <button class="btn btn-primary" onclick="saveParkingLot()">保存</button>
        `;
    } else if (type === 'vehicle') {
        document.getElementById('modal-body').innerHTML = `
            <div class="form-group"><label>VIN</label><input type="text" id="vehicle-vin"></div>
            <div class="form-group"><label>状态</label>
                <select id="vehicle-status">
                    <option value="巡游中">巡游中</option>
                    <option value="调度中">调度中</option>
                    <option value="接乘中">接乘中</option>
                    <option value="履约中">履约中</option>
                    <option value="维护中">维护中</option>
                    <option value="休息中">休息中</option>
                </select>
            </div>
            <div class="form-group"><label>电量(%)</label><input type="number" id="vehicle-battery" min="0" max="100"></div>
            <div class="form-group"><label>经度</label><input type="number" step="0.000001" id="vehicle-lng"></div>
            <div class="form-group"><label>纬度</label><input type="number" step="0.000001" id="vehicle-lat"></div>
            <button class="btn btn-primary" onclick="saveVehicle()">保存</button>
        `;
    } else if (type === 'station') {
        document.getElementById('modal-body').innerHTML = `
            <div class="form-group"><label>名称</label><input type="text" id="station-name"></div>
            <div class="form-group"><label>类型</label>
                <select id="station-type">
                    <option value="商圈">商圈</option>
                    <option value="景区">景区</option>
                    <option value="医院">医院</option>
                    <option value="社区">社区</option>
                    <option value="交通枢纽">交通枢纽</option>
                </select>
            </div>
            <div class="form-group"><label>经度</label><input type="number" step="0.000001" id="station-lng"></div>
            <div class="form-group"><label>纬度</label><input type="number" step="0.000001" id="station-lat"></div>
            <div class="form-group"><label>备注</label><input type="text" id="station-remark"></div>
            <button class="btn btn-primary" onclick="saveStation()">保存</button>
        `;
    } else if (type === 'order') {
        document.getElementById('modal-body').innerHTML = `
            <div class="form-group"><label>起始站ID</label><input type="number" id="order-origin"></div>
            <div class="form-group"><label>终止站ID</label><input type="number" id="order-dest"></div>
            <div class="form-group"><label>预计收益</label><input type="number" step="0.01" id="order-revenue"></div>
            <button class="btn btn-primary" onclick="saveOrder()">保存</button>
        `;
    } else if (type === 'assign') {
        document.getElementById('modal-body').innerHTML = `<p>加载可用车辆中...</p>`;
        API.getVehiclesByStatus('巡游中').then(vehicles => {
            if (vehicles.length === 0) {
                document.getElementById('modal-body').innerHTML = '<p>没有可用的巡游车辆</p>';
                return;
            }
            document.getElementById('modal-body').innerHTML = `
                <div class="form-group"><label>选择车辆</label>
                    <select id="assign-vehicle">${vehicles.map(v => `<option value="${v.id}">${v.id} - ${v.vin}</option>`).join('')}</select>
                </div>
                <button class="btn btn-primary" onclick="assignOrder('${id}')">派单</button>
            `;
        });
    }
}

// 关闭模态框
function closeModal() {
    document.getElementById('modal').classList.remove('show');
}

// 导出全局函数
window.showModal = showModal;
window.closeModal = closeModal;
