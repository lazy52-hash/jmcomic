package com.lazy.jmcomic.api.v2.pojo;

import java.util.List;

/**
 * JMComic V2 登录响应数据
 * 对应 JSON 示例：
 * {
 *   "uid": "20574186",
 *   "username": "lazyqajmc",
 *   "email": "1254586216@qq.com",
 *   "emailverified": "yes",
 *   "photo": "nopic-Male.gif?v=0",
 *   "fname": "",
 *   "gender": "Male",
 *   "message": "Welcome lazyqajmc!",
 *   "coin": 200,
 *   "album_favorites": 0,
 *   "s": "jj93ji8n66c8en7u21p3tondp6",
 *   "level_name": "游侠",
 *   "level": 2,
 *   "nextLevelExp": 420,
 *   "exp": "385",
 *   "expPercent": 91.66666666666666,
 *   "badges": [],
 *   "album_favorites_max": 400,
 *   "ad_free": false,
 *   "ad_free_before": "0000-00-00 00:00:00",
 *   "charge": "14/30",
 *   "jar": "0/1",
 *   "invitation_qrcode": "https://jm18c-oec.cc/uiqrcode?uicode=b3ff488186",
 *   "invitation_url": "https://jm18c-oec.cc/signup?uicode=b3ff488186",
 *   "invited_cnt": "0",
 *   "jwttoken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
 * }
 */
public record LoginInfo(
        // 用户基本信息
        String uid,
        String username,
        String email,
        String emailverified,
        String photo,
        String fname,
        String gender,
        String message,

        // 货币和收藏
        Integer coin,
        Integer album_favorites,

        // 会话标识
        String s,

        // 等级和经验
        String level_name,
        Integer level,
        Integer nextLevelExp,
        String exp,
        Double expPercent,

        // 徽章
        List<String> badges,

        // 收藏限制
        Integer album_favorites_max,

        // 广告相关
        Boolean ad_free,
        String ad_free_before,

        // 充电/订阅
        String charge,
        String jar,

        // 邀请相关
        String invitation_qrcode,
        String invitation_url,
        String invited_cnt,

        // JWT Token
        String jwttoken
) {
}
