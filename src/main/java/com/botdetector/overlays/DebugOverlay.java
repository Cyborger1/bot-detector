package com.botdetector.overlays;

import com.botdetector.BotDetectorConfig;
import com.botdetector.BotDetectorPlugin;
import com.botdetector.model.PlayerSighting;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ColorUtil;

public class DebugOverlay extends Overlay
{
	private static final int MAX_DRAW_DISTANCE = 50;

	private final Client client;
	private final BotDetectorConfig config;
	private final BotDetectorPlugin plugin;

	@Getter
	@Setter
	private boolean overlayActive;

	@Inject
	private DebugOverlay(Client client, BotDetectorConfig config, BotDetectorPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);

		overlayActive = false;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!overlayActive)
		{
			return null;
		}

		Collection<PlayerSighting> sightings;
		synchronized (plugin.getSightingTable())
		{
			sightings = Arrays.stream(client.getMapRegions())
				.mapToObj(plugin.getSightingTable().columnMap()::get)
				.filter(Objects::nonNull)
				.flatMap(t -> t.values().stream())
				.collect(Collectors.toList());
		}

		boolean isInstanced = client.isInInstancedRegion();
		for (final PlayerSighting sighting : sightings)
		{
			int plane = isInstanced ? client.getPlane() : sighting.getPlane();
			if (plane != client.getPlane())
			{
				continue;
			}

			WorldPoint worldPoint = WorldPoint.fromRegion(
				sighting.getRegionID(),
				sighting.getWorldX() % 64,
				sighting.getWorldY() % 64,
				plane
			);

			drawTile(graphics, worldPoint, ColorUtil.fromObject(sighting.getPlayerName()), sighting.getPlayerName());
		}

		return null;
	}

	private void drawTile(Graphics2D graphics, WorldPoint point, Color color, @Nullable String label)
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return;
		}

		WorldPoint playerLocation = localPlayer.getWorldLocation();

		for (WorldPoint wp : WorldPoint.toLocalInstance(client, point))
		{
			if (wp.distanceTo(playerLocation) >= MAX_DRAW_DISTANCE)
			{
				continue;
			}

			LocalPoint lp = LocalPoint.fromWorld(client, wp);
			if (lp == null)
			{
				continue;
			}

			Polygon poly = Perspective.getCanvasTilePoly(client, lp);
			if (poly != null)
			{
				OverlayUtil.renderPolygon(graphics, poly, color);
			}

			if (label != null && !label.isEmpty())
			{
				Point canvasTextLocation = Perspective.getCanvasTextLocation(client, graphics, lp, label, 0);
				if (canvasTextLocation != null)
				{
					OverlayUtil.renderTextLocation(graphics, canvasTextLocation, label, color);
				}
			}
		}
	}
}
