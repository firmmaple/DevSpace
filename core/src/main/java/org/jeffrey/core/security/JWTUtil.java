package org.jeffrey.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTUtil {
    private final String secretKey = "5367566B59703373357638792F423F4528482B4D6251655468576D5A71347437";

    private final long tokenExpire = 1000 * 60 * 60 * 24 * 2;   // token过期时间, 2 day

    private final long tokenDetect = 1000 * 60 * 60 * 24 * 1;   // token续期检查, 1 day

    private final long tokenRenew = 1000 * 60 * 60 * 24 * 2;    // token续期时间, 1 day

    public static final String onlineKey = "online-token:";

//    private final RedisUtils redisUtils;

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, tokenExpire);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        Date currentTime = new Date(System.currentTimeMillis());
        Date expireTime = new Date(System.currentTimeMillis() + expiration);
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(currentTime)
                .setExpiration(expireTime)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从token中获取用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 获取token终止时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 验证token是否有效
     *
     * @param token       客户端传入的token
     * @param userDetails 从数据库中查询出的用户信息
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // 验证 Token 是否有效
    public boolean validate(String token) {
        try {
            // 尝试解析 Token，如果签名无效、过期或格式错误会抛出异常
            Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token);
            // 也可以额外检查是否过期，虽然解析时已包含此检查
            // return !isExpired(token);
            return true;
        } catch (Exception e) {
            // Token 无效 (签名错误, 过期等)
            return false;
        }
    }

    /**
     * token是否过期，即终止日期是否早于当前时刻
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从token中去除payload
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 获取登录用用户的Redis Key
     *
     * @param token JWT Token
     * @return 该用户的存储与Redis中的Key
     */
//    public String getRedisLoginKey(String token) {
//        String username = extractUsername(token);
//        String md5Token = DigestUtil.md5Hex(token);
//        return onlineKey + username + "-" + md5Token;
//    }
//
//    public void renewal(String token) {
//        String loginKey = getRedisLoginKey(token);
//        redisUtils.expire(loginKey, tokenRenew, TimeUnit.MILLISECONDS);
//    }
}
