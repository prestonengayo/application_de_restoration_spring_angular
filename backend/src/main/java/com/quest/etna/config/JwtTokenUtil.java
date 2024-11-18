package com.quest.etna.config;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil implements Serializable {

    
    private final long jwtExpiration = 3600 * 24 * 1000; // 24 heures en millisecondes
    private final long refreshTokenExpiration = 3600 * 24 * 7 * 1000; // 7 jours en millisecondes


    private final String secretKey = "IyNldG5hX3F1ZXN0XzIwMjMjI2V0bmFfcXVlc3RfMjAyMyMjZXRuYV9xdWVzdF8yMDIzIyM="; // etna_quest

    //retrieve username from jwt token
    public String getUsernameFromToken(String token) {
    	return extractClaim(token, Claims::getSubject);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	//generate token for user
	public String generateToken(String username) {
		Map<String, Object> claims = new HashMap<>();
		return generateToken(claims, username.toLowerCase());
	}

	public String generateToken(Map<String, Object> extraClaims, String subject) {
		return buildToken(extraClaims, subject, jwtExpiration);
	}
	
	
	public String generateRefreshToken(String username) {
	    return buildToken(new HashMap<>(), username, refreshTokenExpiration);
	}

	public boolean validateRefreshToken(String token) {
	    return !isTokenExpired(token);
	}



	//while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
	private String buildToken( Map<String, Object> extraClaims, String subject, long expiration) {
		return Jwts
			.builder()
			.setClaims(extraClaims)
			.setSubject(subject)
			.setIssuedAt(new Date(System.currentTimeMillis()))
			.setExpiration(new Date(System.currentTimeMillis() + expiration))
			.signWith(getSignInKey(), SignatureAlgorithm.HS256)
			.compact();
	}

	//validate token
	public boolean validateToken(String token, UserDetails userDetails) {
		final String username = getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
	}

	//check if the token has expired
	public boolean isTokenExpired(String token) {
	    return extractExpiration(token).before(new Date());
	}

	//retrieve expiration date from jwt token
	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	//for retrieving any information from token we will need the secret key
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
	

}