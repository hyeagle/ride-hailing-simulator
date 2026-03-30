package org.example.config;

import org.example.entity.*;
import org.example.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

// @Component  // 已改用 data.sql 初始化数据
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ParkingLotRepository parkingLotRepository;
    private final StationRepository stationRepository;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;

    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        if (parkingLotRepository.count() > 0) {
            log.info("数据已存在，跳过初始化");
            return;
        }

        log.info("开始初始化数据...");
        initParkingLots();
        initStations();
        initVehicles();
        log.info("数据初始化完成");
    }

    private void initParkingLots() {
        parkingLotRepository.save(ParkingLot.builder()
                .id("P001").name("停车场001").gisGrid("GIS-GRID-001").capacity(30)
                .latitude(32.058123).longitude(118.795467).remark("仿真专用停车场").build());
        parkingLotRepository.save(ParkingLot.builder()
                .id("P002").name("停车场002").gisGrid("GIS-GRID-004").capacity(50)
                .latitude(32.046789).longitude(118.802345).remark("仿真专用停车场").build());
        parkingLotRepository.save(ParkingLot.builder()
                .id("P003").name("停车场003").gisGrid("GIS-GRID-007").capacity(40)
                .latitude(32.060123).longitude(118.788765).remark("仿真专用停车场").build());
        log.info("初始化停车场完成，共3个");
    }

    private void initStations() {
        Station.StationType[] types = Station.StationType.values();
        String[][] stationData = {
                {"NJJD0827A", "中山陵景区入口站", "32.059344", "118.796624", "景区", "景区入口旁，高频上下客点"},
                {"NJSC1015B", "新街口商圈中心站", "32.045678", "118.801234", "商圈", "商圈核心区，平峰时段订单集中"},
                {"NJYY0608C", "江苏省人民医院六号门站", "32.061234", "118.789012", "医院", "医院六号门附近，就医出行高频点"},
                {"NJSH0922D", "万科城社区正门站", "31.998765", "118.854321", "社区", "大型社区门口，早晚高峰订单集中"},
                {"NJJT1210E", "南京地铁1号线新街口站出口站", "32.012345", "118.823456", "交通枢纽", "地铁出口旁，换乘出行高频点"}
        };

        for (String[] data : stationData) {
            stationRepository.save(Station.builder()
                    .id(data[0]).name(data[1])
                    .latitude(Double.parseDouble(data[2])).longitude(Double.parseDouble(data[3]))
                    .type(Station.StationType.valueOf(data[4])).remark(data[5]).build());
        }

        // 生成2000个站点
        String[] prefixes = {"商圈", "景区", "医院", "社区", "交通枢纽", "学校", "公园", "购物中心", "写字楼", "住宅区"};
        for (int i = 6; i <= 2000; i++) {
            double lat = 31.2340 + random.nextDouble() * (32.6200 - 31.2340);
            double lng = 118.3000 + random.nextDouble() * (119.2000 - 118.3000);
            Station.StationType type = types[random.nextInt(types.length)];
            String prefix = prefixes[random.nextInt(prefixes.length)];
            stationRepository.save(Station.builder()
                    .id(generateStationId(i))
                    .name(prefix + "站点" + i)
                    .latitude(Math.round(lat * 1000000.0) / 1000000.0)
                    .longitude(Math.round(lng * 1000000.0) / 1000000.0)
                    .type(type)
                    .build());
        }
        log.info("初始化站点完成，共{}个", stationRepository.count());
    }

    private void initVehicles() {
        String[][] vehicleData = {
                {"C0001", "LHGCM82633A123456", "履约中", "nj-202603270830-0001", "78", "32.059344", "118.796624", "GIS-GRID-001"},
                {"C0002", "LHGCM82633A123457", "巡游中", "-", "65", "32.045678", "118.801234", "GIS-GRID-002"},
                {"C0003", "LHGCM82633A123458", "维护中", "-", "50", "32.061234", "118.789012", "GIS-GRID-003"},
                {"C0004", "LHGCM82633A123459", "履约中", "nj-202603271410-0002", "42", "32.001234", "118.845678", "GIS-GRID-004"},
                {"C0005", "LHGCM82633A123460", "巡游中", "-", "89", "31.997654", "118.853210", "GIS-GRID-005"}
        };

        for (String[] data : vehicleData) {
            Vehicle.VehicleStatus status = Vehicle.VehicleStatus.valueOf(data[2]);
            String orderId = "-".equals(data[3]) ? null : data[3];
            vehicleRepository.save(Vehicle.builder()
                    .id(data[0]).vin(data[1]).status(status).orderId(orderId)
                    .batteryPercent(Integer.parseInt(data[4]))
                    .latitude(Double.parseDouble(data[5])).longitude(Double.parseDouble(data[6]))
                    .gisGrid(data[7]).build());
        }
        log.info("初始化车辆完成，共{}辆", vehicleRepository.count());
    }

    private String generateStationId(int index) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder id = new StringBuilder("NJ");
        id.append(chars.charAt(random.nextInt(chars.length())));
        id.append(chars.charAt(random.nextInt(chars.length())));
        id.append(String.format("%04d", index % 10000));
        id.append(chars.charAt(random.nextInt(chars.length())));
        return id.toString();
    }
}
