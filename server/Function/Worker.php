<?php


namespace Im\Func;


define('EARTH_RADIUS', 6371);       // 定义 地球半径
trait Worker
{
    /**
     * * https://www.jb51.net/article/83974.htm
     * @param $lat float 经度
     * @param $lng float 纬度
     * @param $distance float 距离
     * 获取附近的人 范围坐标
     * @return array
     */
    public static function getNearBy(float $lat, float $lng, float $distance) {
        $dlng = 2 * asin(sin($distance / (2 * EARTH_RADIUS)) / cos(deg2rad($lat)));
        $dlng = rad2deg($dlng);
        $dlat = $distance/EARTH_RADIUS;
        $dlat = rad2deg($dlat);

//        return [
//            'left-top' => [
//                'lat'=>$lat + $dlat,
//                'lng'=>$lng - $dlng,
//            ],
//            'right-top' => [
//                'lat'=>$lat + $dlat,
//                'lng'=>$lng + $dlng,
//            ],
//            'left-bottom' => [
//                'lat'=>$lat - $dlat,
//                'lng'=>$lng - $dlng,
//            ],
//            'right-bottom' => [
//                'lat'=>$lat - $dlat,
//                'lng'=>$lng + $dlng,
//            ],
//        ];
        return [
            'lat' => [$lat - $dlat, $lat + $dlat],      // 经度范围
            'lng' => [$lng - $dlng, $lng + $dlng],      // 纬度范围
        ];

    }
}