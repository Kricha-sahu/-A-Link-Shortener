package com.linkshortener.Link_Shortener.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class LinkService {

	private Map<String, Link> links = new HashMap<>();

	public boolean isUrlExists(String url) {
		return links.values().stream().anyMatch(link -> link.getOriginalUrl().equals(url));
	}

	public String getShortUrl(String originalUrl) {
		return links.values().stream().filter(link -> link.getOriginalUrl().equals(originalUrl)).findFirst()
				.map(Link::getShortUrl).orElse(null);
	}

	public String generateShortUrl(String originalUrl) {
		String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder shortUrlBuilder = new StringBuilder();
		Random random = new Random();

		for (int i = 0; i < 8; i++) {
			int index = random.nextInt(characters.length());
			shortUrlBuilder.append(characters.charAt(index));
		}

		String shortUrl = shortUrlBuilder.toString();

		if (links.containsKey(shortUrl)) {
			return generateShortUrl(originalUrl);
		}

		return shortUrl;
	}

	public void saveLink(String shortUrl, String originalUrl) {
		LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
		Link link = new Link(shortUrl, originalUrl, expirationTime);
		links.put(shortUrl, link);
	}

	public String getOriginalUrl(String shortUrl) {
		Link link = links.get(shortUrl);
		return (link != null && !isExpired(shortUrl)) ? link.getOriginalUrl() : null;
	}

	public boolean isExpired(String shortUrl) {
		Link link = links.get(shortUrl);
		if (link != null) {
			LocalDateTime expirationTime = link.getExpirationTime();
			LocalDateTime currentTime = LocalDateTime.now();
			return expirationTime.isBefore(currentTime) || currentTime.isAfter(expirationTime.plusMinutes(5));
		}
		return false;
	}

	private String generateRandomString() {
		return UUID.randomUUID().toString().substring(0, 8);
	}

	static class Link {
		private String shortUrl;
		private String originalUrl;
		private LocalDateTime expirationTime;

		public Link(String shortUrl, String originalUrl, LocalDateTime expirationTime) {
			this.shortUrl = shortUrl;
			this.originalUrl = originalUrl;
			this.expirationTime = expirationTime;
		}

		public String getShortUrl() {
			return shortUrl;
		}

		public String getOriginalUrl() {
			return originalUrl;
		}

		public LocalDateTime getExpirationTime() {
			return expirationTime;
		}
	}
}
