package com.botdetector.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@RequiredArgsConstructor
public enum FlagResponse
{
	@SerializedName("u")
	UNFLAGGED,
	@SerializedName("b")
	BOT,
	@SerializedName("n")
	NOT_BOT
}
