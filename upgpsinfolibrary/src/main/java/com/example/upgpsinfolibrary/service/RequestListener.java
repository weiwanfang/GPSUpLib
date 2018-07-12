package com.example.upgpsinfolibrary.service;

public abstract interface RequestListener {

	public abstract void onComplete(int tag, String json);

	public abstract void onException(String json);
}
