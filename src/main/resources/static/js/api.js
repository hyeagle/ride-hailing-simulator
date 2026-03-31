/**
 * API 调用模块
 */

const API = {
    // 停车场相关
    getParkingLots: () => fetch('/api/parking-lots').then(r => r.json()),
    getParkingLot: (id) => fetch(`/api/parking-lots/${id}`).then(r => r.json()),
    createParkingLot: (data) => fetch('/api/parking-lots', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    }),
    updateParkingLot: (id, data) => fetch(`/api/parking-lots/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    }),
    deleteParkingLot: (id) => fetch(`/api/parking-lots/${id}`, { method: 'DELETE' }),

    // 车辆相关
    getVehicles: () => fetch('/api/vehicles').then(r => r.json()),
    getVehicle: (id) => fetch(`/api/vehicles/${id}`).then(r => r.json()),
    searchVehicles: (params) => fetch(`/api/vehicles/search?${params.toString()}`).then(r => r.json()),
    getVehiclesByStatus: (status) => fetch(`/api/vehicles/status/${encodeURIComponent(status)}`).then(r => r.json()),
    createVehicle: (data) => fetch('/api/vehicles', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    }),
    updateVehicle: (id, data) => fetch(`/api/vehicles/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    }),
    deleteVehicle: (id) => fetch(`/api/vehicles/${id}`, { method: 'DELETE' }),
    powerOnVehicles: (count) => fetch(`/api/vehicles/power-on?count=${count}`, { method: 'POST' }).then(r => r.json()),

    // 站点相关
    getStations: () => fetch('/api/stations').then(r => r.json()),
    getStationsByType: (type) => fetch(`/api/stations/type/${encodeURIComponent(type)}`).then(r => r.json()),
    getStation: (id) => fetch(`/api/stations/${id}`).then(r => r.json()),
    createStation: (data) => fetch('/api/stations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    }),
    updateStation: (id, data) => fetch(`/api/stations/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    }),
    deleteStation: (id) => fetch(`/api/stations/${id}`, { method: 'DELETE' }),

    // 订单相关
    getOrders: () => fetch('/api/orders').then(r => r.json()),
    getOrdersByStatus: (status) => fetch(`/api/orders/status/${encodeURIComponent(status)}`).then(r => r.json()),
    createOrder: (data) => fetch('/api/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    }),
    assignOrder: (orderId, vehicleId) => fetch(`/api/orders/${orderId}/assign/${vehicleId}`, { method: 'POST' }),
    completeOrder: (id) => fetch(`/api/orders/${id}/complete`, { method: 'POST' }),
    cancelOrder: (id) => fetch(`/api/orders/${id}/cancel`, { method: 'POST' }),

    // 派单相关
    getOrderDispatchInfo: () => fetch('/api/order-dispatch/info').then(r => r.json()),
    triggerOrderDispatch: () => fetch('/api/order-dispatch/trigger', { method: 'POST' }).then(r => r.text()),
    simulateOrders: (count) => fetch(`/api/order-dispatch/simulate?count=${count}`, { method: 'POST' }).then(r => r.json()),
    notifyArrivedPickup: (vehicleId) => fetch(`/api/order-dispatch/arrived-pickup/${vehicleId}`, { method: 'POST' }),
    notifyArrivedDestination: (vehicleId) => fetch(`/api/order-dispatch/arrived-destination/${vehicleId}`, { method: 'POST' }),

    // 调度相关
    getDispatchInfo: () => fetch('/api/dispatch/info').then(r => r.json()),
    triggerDispatch: () => fetch('/api/dispatch/trigger', { method: 'POST' }).then(r => r.text()),
    notifyDispatchArrived: (vehicleId) => fetch(`/api/dispatch/arrived/${vehicleId}`, { method: 'POST' }),

    // 路线规划
    planRoute: (origin, destination) => fetch('/api/route/plan', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ origin, destination })
    }).then(r => r.json())
};

// 导出全局
window.API = API;
