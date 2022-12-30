package com.botdetector.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@RequiredArgsConstructor
public enum FlagResponse
{
	UNFLAGGED,
	BOT,
	NOT_BOT
}
