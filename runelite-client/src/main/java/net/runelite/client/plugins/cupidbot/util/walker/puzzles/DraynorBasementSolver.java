/*
 * Copyright (c) 2025, Microbot
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.cupidbot.util.walker.puzzles;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.plugins.cupidbot.CupidBot;
import net.runelite.client.plugins.cupidbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.cupidbot.util.player.Rs2Player;
import net.runelite.client.plugins.cupidbot.util.walker.Rs2Walker;

import java.util.ArrayDeque;

import static net.runelite.client.plugins.cupidbot.util.Global.sleepUntil;

/**
 * Solves the Draynor Manor (Ernest the Chicken) basement lever puzzle during a web-walk.
 *
 * The 9 puzzle doors are registered as varbit-conditional transports in transports.tsv. This
 * solver sets the levers so those transports become available before the walker tries to cross
 * the puzzle doors.
 */
@Slf4j
public final class DraynorBasementSolver {
    private DraynorBasementSolver() {
    }

    private static volatile boolean active = false;

    private static final int ENTRANCE = 0;
    private static final int CD = 1;
    private static final int SS = 2;
    private static final int NS = 3;
    private static final int SW = 4;
    private static final int EF = 5;
    private static final int OIL = 6;

    private static final int[][] ROOM_BOX = {
            {3100, 9745, 3118, 9757},
            {3105, 9758, 3112, 9767},
            {3100, 9758, 3104, 9762},
            {3100, 9763, 3104, 9767},
            {3096, 9758, 3099, 9762},
            {3096, 9763, 3099, 9767},
            {3090, 9753, 3099, 9757},
    };

    private static final int[] LEVER_VARBIT = {
            VarbitID.ERNESTLEVER_A, VarbitID.ERNESTLEVER_B, VarbitID.ERNESTLEVER_C,
            VarbitID.ERNESTLEVER_D, VarbitID.ERNESTLEVER_E, VarbitID.ERNESTLEVER_F};
    private static final int[] LEVER_OBJ = {
            ObjectID.LEVERA, ObjectID.LEVERB, ObjectID.LEVERC,
            ObjectID.LEVERD, ObjectID.LEVERE, ObjectID.LEVERF};
    private static final int[] LEVER_ROOM = {ENTRANCE, ENTRANCE, CD, CD, EF, EF};
    private static final WorldPoint[] LEVER_TILE = {
            new WorldPoint(3108, 9745, 0), new WorldPoint(3118, 9752, 0), new WorldPoint(3112, 9760, 0),
            new WorldPoint(3108, 9767, 0), new WorldPoint(3097, 9767, 0), new WorldPoint(3096, 9765, 0)};

    private static final WorldPoint[] ROOM_TILE = {
            new WorldPoint(3115, 9752, 0), new WorldPoint(3110, 9762, 0), new WorldPoint(3102, 9760, 0),
            new WorldPoint(3102, 9765, 0), new WorldPoint(3098, 9760, 0), new WorldPoint(3097, 9766, 0),
            new WorldPoint(3093, 9755, 0)};

    // Doors: {roomA, roomB, mask, requiredValue}. A combo bit is set when that lever is down.
    private static final int[][] DOORS = {
            {ENTRANCE, CD, 0b100111, 0b000011},
            {CD, SS, 0b110101, 0b000001},
            {ENTRANCE, SS, 0b011000, 0b001000},
            {SS, SW, 0b011000, 0b001000},
            {EF, SW, 0b101010, 0b001000},
            {EF, NS, 0b101010, 0b101000},
            {CD, NS, 0b110001, 0b110000},
            {SS, NS, 0b110000, 0b100000},
            {ENTRANCE, OIL, 0b101100, 0b101100},
    };

    private static final int LEVER_PULL_TIMEOUT_MS = 4000;

    public static boolean isBasementTarget(WorldPoint point) {
        return point != null && point.getPlane() == 0
                && point.getX() >= 3088 && point.getX() <= 3120
                && point.getY() >= 9744 && point.getY() <= 9768;
    }

    private static int roomOf(WorldPoint point) {
        if (point == null) {
            return -1;
        }
        for (int room = 0; room < ROOM_BOX.length; room++) {
            int[] box = ROOM_BOX[room];
            if (point.getX() >= box[0] && point.getX() <= box[2]
                    && point.getY() >= box[1] && point.getY() <= box[3]) {
                return room;
            }
        }
        return -1;
    }

    private static int readCombo() {
        int combo = 0;
        for (int i = 0; i < 6; i++) {
            if (CupidBot.getVarbitValue(LEVER_VARBIT[i]) == 1) {
                combo |= (1 << i);
            }
        }
        return combo;
    }

    private static int[] firstAction(int startCombo, int startRoom, int targetRoom) {
        if (startRoom < 0 || targetRoom < 0 || startRoom == targetRoom) {
            return null;
        }
        final int stateCount = 64 * 7;
        int[][] first = new int[stateCount][];
        boolean[] seen = new boolean[stateCount];
        int start = startCombo * 7 + startRoom;
        seen[start] = true;
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            int state = queue.poll();
            int combo = state / 7;
            int room = state % 7;
            if (room == targetRoom) {
                return first[state];
            }
            for (int lever = 0; lever < 6; lever++) {
                if (LEVER_ROOM[lever] == room) {
                    int nextState = (combo ^ (1 << lever)) * 7 + room;
                    if (!seen[nextState]) {
                        seen[nextState] = true;
                        first[nextState] = (state == start) ? new int[]{0, lever} : first[state];
                        queue.add(nextState);
                    }
                }
            }
            for (int[] door : DOORS) {
                if ((room == door[0] || room == door[1]) && (combo & door[2]) == door[3]) {
                    int other = room == door[0] ? door[1] : door[0];
                    int nextState = combo * 7 + other;
                    if (!seen[nextState]) {
                        seen[nextState] = true;
                        first[nextState] = (state == start) ? new int[]{1, other} : first[state];
                        queue.add(nextState);
                    }
                }
            }
        }
        return null;
    }

    public static void solveIfNeeded(WorldPoint target) {
        if (active || !isBasementTarget(target) || !CupidBot.isLoggedIn()) {
            return;
        }
        WorldPoint player = Rs2Player.getWorldLocation();
        if (!isBasementTarget(player)) {
            return;
        }
        int targetRoom = roomOf(target);
        if (targetRoom < 0 || roomOf(player) == targetRoom) {
            return;
        }
        active = true;
        try {
            log.info("[DraynorBasement] solving toward room {}", targetRoom);
            for (int step = 0; step < 80; step++) {
                int room = roomOf(Rs2Player.getWorldLocation());
                if (room < 0 || room == targetRoom) {
                    return;
                }
                int[] action = firstAction(readCombo(), room, targetRoom);
                if (action == null) {
                    log.warn("[DraynorBasement] no path from room {} to room {}", room, targetRoom);
                    return;
                }
                if (action[0] == 0) {
                    int lever = action[1];
                    if (!Rs2Walker.walkTo(LEVER_TILE[lever], 1)) {
                        log.warn("[DraynorBasement] could not reach lever {}", (char) ('A' + lever));
                        return;
                    }
                    final int varbit = LEVER_VARBIT[lever];
                    final int before = CupidBot.getVarbitValue(varbit);
                    if (!Rs2GameObject.interact(LEVER_OBJ[lever], "Pull")
                            || !sleepUntil(() -> CupidBot.getVarbitValue(varbit) != before, LEVER_PULL_TIMEOUT_MS)) {
                        log.warn("[DraynorBasement] lever {} pull did not register", (char) ('A' + lever));
                        return;
                    }
                } else {
                    final int nextRoom = action[1];
                    boolean crossed = false;
                    for (int attempt = 0; attempt < 3 && !crossed; attempt++) {
                        Rs2Walker.walkTo(ROOM_TILE[nextRoom], 1);
                        crossed = roomOf(Rs2Player.getWorldLocation()) == nextRoom
                                || sleepUntil(() -> roomOf(Rs2Player.getWorldLocation()) == nextRoom, 700);
                    }
                    if (!crossed) {
                        log.warn("[DraynorBasement] could not cross into room {}; deferring to walker", nextRoom);
                        return;
                    }
                }
                sleepUntil(() -> !Rs2Player.isMoving(), 400);
            }
        } finally {
            active = false;
        }
    }
}
