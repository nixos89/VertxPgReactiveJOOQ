package com.ns.vertx.pg;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.vertx.core.json.JsonObject;

public class Category {
	private long categoryId = -1;
	private String name;
	
	private boolean isDeleted;

	public Category() {}

	public Category(String name, boolean isDeleted) {
		this.name = name;
		this.isDeleted = isDeleted;
	}
	
	public Category(long id, String name, boolean isDeleted) {
		this.categoryId = id;
		this.name = name;
		this.isDeleted = isDeleted;
	}


	public Category(JsonObject json) {
		this(
			json.getInteger("category_id", -1), 
			json.getString("name"), 
			json.getBoolean("is_deleted")
		);
	}

	@JsonProperty("category_id")
	public long getCategoryId() {
		return categoryId;
	}


	@JsonProperty("name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("is_deleted")
	public boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	
}
