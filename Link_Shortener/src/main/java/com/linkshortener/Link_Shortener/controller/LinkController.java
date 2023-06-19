package com.linkshortener.Link_Shortener.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import com.linkshortener.Link_Shortener.service.LinkService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LinkController {

	@Autowired
	private LinkService linkService;

	@PostMapping("/shorten")
	public ResponseEntity<?> shortenUrl(@RequestBody LinkRequest linkRequest) {
		String originalUrl = linkRequest.getUrl();

		if (!isValidUrl(originalUrl)) {
			return ResponseEntity.badRequest().body("Invalid URL.");
		}

		if (linkService.isUrlExists(originalUrl)) {
			String shortUrl = linkService.getShortUrl(originalUrl);
			return ResponseEntity.ok(new ShortUrlResponse(shortUrl));
		}

		String shortUrl = linkService.generateShortUrl(originalUrl);

		linkService.saveLink(shortUrl, originalUrl);

		return ResponseEntity.ok(new ShortUrlResponse(shortUrl));
	}

	@GetMapping("/{shortUrl}")
	public ResponseEntity<?> redirectToOriginalUrl(@PathVariable String shortUrl, HttpServletRequest request,
			HttpServletResponse response) {
		String originalUrl = linkService.getOriginalUrl(shortUrl);

		if (originalUrl == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("URL does not exist. Create the URL <a href=\"/create-url\">here</a>.");
		}

		if (linkService.isExpired(shortUrl)) {
			return ResponseEntity.status(HttpStatus.GONE).body("URL has expired.");
		}

		try {
			URI originalUri = new URI(originalUrl);

			if (!originalUri.isAbsolute()) {
				String baseUrl = getBaseUrl(request);
				String redirectUrl = baseUrl + originalUrl;

				response.sendRedirect(redirectUrl);
				return ResponseEntity.ok().build();
			}

			String redirectUrl = originalUri.toURL().toString();
			response.sendRedirect(redirectUrl);
			return ResponseEntity.ok().build();
		} catch (URISyntaxException | IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	private String getBaseUrl(HttpServletRequest request) {
		String scheme = request.getScheme();
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();
		String contextPath = request.getContextPath();

		return scheme + "://" + serverName + ":" + serverPort + contextPath;
	}

	private boolean isValidUrl(String url) {
		try {
			if (!url.startsWith("http://") && !url.startsWith("https://")) {
				url = "http://" + url;
			}

			URI uri = new URI(url);

			if (uri.getScheme() == null || uri.getHost() == null) {
				return false;
			}

			return true;
		} catch (URISyntaxException e) {
			return false;
		}
	}

	@GetMapping("/create-url")
	public String createUrl() {
		return "create-url";
	}

	@GetMapping("/expired-url")
	public String expiredUrl() {
		return "expired-url";
	}

	static class LinkRequest {
		private String url;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}
	}

	static class ShortUrlResponse {
		private String shortUrl;

		public ShortUrlResponse(String shortUrl) {
			this.shortUrl = shortUrl;
		}

		public String getShortUrl() {
			return shortUrl;
		}

		public void setShortUrl(String shortUrl) {
			this.shortUrl = shortUrl;
		}
	}
}