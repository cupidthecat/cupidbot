# Mouse Realism Engine Checklist for Games

This document frames “mouse humanization” as a legitimate mouse-input realism engine

---

## 1. Core Goals

A high-quality mouse engine should feel believable, responsive, configurable, and measurable. It should not just move a cursor from point A to point B. It should model how real mouse input behaves across speed, timing, path shape, input device characteristics, game sensitivity, target size, reaction delay, and user state.

### Primary design goals

- **Natural motion:** Movement should include acceleration, deceleration, small corrections, and non-perfect paths.
- **Responsiveness:** Motion should respect game frame time, input latency, and user expectations.
- **Configurability:** Designers should be able to tune speed, smoothness, randomness, error, delay, and style.
- **Determinism when needed:** QA and replay systems should support fixed random seeds.
- **Debuggability:** Every generated movement should be explainable with overlays, logs, plots, and metrics.

---

## 2. Input and Output Architecture

A strong engine separates the system into layers. This keeps the mouse model easier to test and prevents one giant function from handling everything.

### Recommended layers

1. **Intent layer**
   - Defines the goal: move to UI element, rotate camera, drag item, scroll list, select object, or perform a click.
   - Knows target type, target size, priority, and allowed error.

2. **Planning layer**
   - Chooses movement duration, path shape, acceleration profile, random variation, and correction behavior.
   - Produces a movement plan independent of frame rate.

3. **Trajectory layer**
   - Converts the movement plan into positions or relative deltas over time.
   - Handles curves, speed profile, overshoot, micro-corrections, and jitter.

4. **Device/game translation layer**
   - Converts desired motion into game input units.
   - Handles DPI, sensitivity, raw input, OS scaling, camera yaw/pitch, FOV, and coordinate transforms.

5. **Runtime scheduler**
   - Emits input events at the right time.
   - Handles frame-rate independence, polling intervals, batching, and cancellation.

6. **Debug/telemetry layer**
   - Records path, velocity, acceleration, jerk, error, target size, timing, and seed.

---

## 3. Coordinate Systems to Support

Mouse behavior depends heavily on the coordinate space being controlled. Build explicit support for each coordinate system instead of mixing them.

### Coordinate spaces

- **Screen space**
  - Absolute desktop coordinates.
  - Important for windowed tools, launchers, editors, and UI automation.

- **Window space**
  - Coordinates relative to the game window.
  - Must account for window borders, title bars, scaling, and resizing.

- **Viewport space**
  - Coordinates inside the rendered game viewport.
  - Useful when UI is letterboxed, pillarboxed, or rendered to a sub-viewport.

- **UI layout space**
  - Coordinates in the UI system’s own layout units.
  - Should account for anchors, safe areas, responsive scaling, and DPI scaling.

- **World space**
  - 3D positions projected into screen space.
  - Useful for cursor interaction with world objects.

- **Camera-relative space**
  - Relative deltas used for first-person/third-person camera rotation.
  - Usually not an absolute cursor position.

- **Raw mouse delta space**
  - Device-level relative movement units.
  - Useful for games using raw input.

### Edge cases

- Multiple monitors.
- Different monitor refresh rates.
- Windows/macOS/Linux DPI scaling.
- High-DPI and low-DPI mice.
- Cursor locked to game window.
- Borderless fullscreen versus exclusive fullscreen.
- UI scale changes while moving.
- Camera pitch/yaw limits.
- Letterboxing and aspect-ratio correction.
- Gamepad/mouse mixed input mode.

---

## 4. Core Movement Parameters

These are the basic parameters every high-quality mouse realism engine should expose.

| Parameter | Purpose | Notes |
|---|---|---|
| `base_speed` | Average cursor/camera speed | Should scale with distance and user profile. |
| `min_speed` | Lowest allowed movement speed | Prevents freezing or endless slow movement. |
| `max_speed` | Highest allowed movement speed | Prevents unnatural instant snaps. |
| `acceleration` | How quickly speed increases | Human movement usually starts slower, accelerates, then slows. |
| `deceleration` | How quickly speed drops near target | Important for believable target acquisition. |
| `movement_duration` | Total time from start to target | Should be derived from distance, target size, and profile. |
| `reaction_time` | Delay before movement begins | Useful for UI agents, NPCs, tutorials, and testing. |
| `settle_time` | Small pause before click or next action | Prevents robotic click-on-arrival behavior. |
| `curve_strength` | How far the path bends | Larger movements often contain mild curvature. |
| `path_noise` | Low-frequency path variation | Should be correlated, not pure static jitter. |
| `micro_jitter` | Tiny hand tremor/noise | Should be subtle and context-dependent. |
| `overshoot_chance` | Probability of passing the target | Useful for realism when moving quickly. |
| `overshoot_distance` | How far beyond the target movement may go | Should scale with speed and distance. |
| `correction_count` | Number of corrective sub-movements | Common near small targets. |
| `endpoint_error` | Final allowed miss distance | Should depend on target size and task difficulty. |
| `click_delay` | Delay between arrival and click | Should vary by task and confidence. |
| `button_down_time` | How long button is held | Clicks are not always identical. |
| `scroll_rate` | Wheel speed | Should support stepped and smooth scrolling. |
| `drag_stability` | How steady dragging feels | Dragging needs different noise rules than free movement. |

---

## 5. Human Movement Models

A believable mouse engine should include at least one formal movement model instead of relying on simple linear interpolation.

### 5.1 Fitts’ Law model

Fitts’ Law predicts that movement time increases with distance and decreases with target size. This is useful for choosing realistic movement duration.

Important inputs:

- Distance to target.
- Target width or clickable area.
- Required precision.
- User skill profile.
- Current speed/momentum.

Use this to avoid unrealistic behavior such as moving to a tiny button as quickly as a large panel.

### 5.2 Minimum-jerk trajectory

Real reaching movements often have smooth velocity profiles. A minimum-jerk curve starts slow, speeds up, then slows down naturally.

Useful for:

- UI cursor movement.
- Cinematic cursor movement.
- Tutorial pointer movement.
- NPC-like pointer movement.

### 5.3 Ballistic plus corrective movement

Many real mouse movements happen in two phases:

1. **Ballistic phase**
   - Fast movement toward the general area.
   - Less precise.
   - Higher speed and wider path variation.

2. **Corrective phase**
   - Smaller adjustments near the target.
   - Slower speed.
   - More precision.

This is one of the most important models for natural movement.

### 5.4 Submovement model

Break one movement into several smaller movements:

- Initial move.
- Minor course correction.
- Final correction.
- Optional overshoot recovery.
- Optional click settle.

This helps avoid robotic perfect arcs.

---

## 6. Speed System

Mouse speed should not be a single constant. It should depend on movement distance, target size, user profile, camera sensitivity, and context.

### Speed features to implement

- Distance-based speed scaling.
- Target-size-based slowdown.
- Separate horizontal and vertical speed tuning if needed.
- Different speed profiles for UI, camera movement, dragging, aiming, scrolling, and menu navigation.
- Acceleration at start.
- Deceleration near target.
- Speed clamping.
- Random but bounded speed variation.
- Speed changes over long sessions to simulate fatigue or warm-up.
- Slower movement after large misses.
- Faster movement for repeated familiar targets.
- Reduced speed while dragging or selecting text/items.
- Optional designer-authored speed curves.

### Useful speed curves

- Linear: simple, but often robotic.
- Ease-in/ease-out: good default for UI movement.
- Minimum jerk: very natural for pointer movement.
- Sigmoid: smooth start and stop.
- Exponential ease-out: good for fast initial camera movement followed by precision.
- Piecewise ballistic/corrective: best for realistic targeting.

---

## 7. Curve and Path Generation

The path should rarely be a perfectly straight line unless the movement is tiny or intentionally mechanical.

### Path types

- **Linear path**
  - Good for debugging.
  - Usually not ideal for final natural movement.

- **Quadratic Bézier curve**
  - Simple one-control-point curve.
  - Good for light curvature.

- **Cubic Bézier curve**
  - Two control points.
  - Good for more flexible motion.

- **Catmull-Rom spline**
  - Good for paths through multiple generated waypoints.

- **Minimum-jerk path**
  - Good for smooth, natural motion.

- **Piecewise path**
  - Combines fast movement, correction, and settling.

### Path features

- Randomized control points.
- Curvature based on distance.
- Curvature based on movement angle.
- Lower curvature for tiny movements.
- More curvature for long movements.
- Occasional shallow S-curves.
- Optional midpoint drift.
- Optional endpoint undershoot.
- Optional endpoint overshoot.
- Correction path after overshoot.
- Replanning if target moves.
- Obstacle avoidance for UI panels or blocked regions.
- Screen-edge avoidance.
- Constraint to stay inside game window.

### Curve realism notes

- Do not use perfectly symmetrical curves every time.
- Do not randomize every point independently; that creates noisy, fake-looking motion.
- Prefer low-frequency correlated variation across the path.
- The cursor should usually become more precise near the end.

---

## 8. Randomization System

Randomness should make motion less repetitive, not chaotic. High-quality randomness is controlled, correlated, and context-aware.

### Randomization types

| Type | Use | Warning |
|---|---|---|
| Uniform random | Simple bounded variation | Can look artificial if overused. |
| Gaussian random | Human-like centered variation | Clamp outliers. |
| Log-normal random | Reaction times and delays | Good for positive-only values. |
| Perlin/simplex noise | Smooth path drift | Better than per-point jitter. |
| Pink noise | Natural low-frequency variation | Useful for long movements. |
| Seeded random | Replays and QA | Allows deterministic reproduction. |
| Profile-based random | Different users/styles | Keeps behavior consistent per profile. |

### Things to randomize

- Reaction time.
- Movement duration.
- Initial delay.
- Curve direction.
- Curve strength.
- Midpoint offset.
- Speed multiplier.
- Acceleration timing.
- Deceleration timing.
- Overshoot chance.
- Overshoot distance.
- Correction count.
- Correction duration.
- Click delay.
- Button hold duration.
- Scroll timing.
- Drag wobble.
- Endpoint error.
- Pause between actions.
- Occasional hesitation.

### Randomization rules

- Keep random values bounded.
- Make randomness depend on context.
- Use correlated noise for paths.
- Use fixed seeds for replay/testing.
- Use different profiles for different “users.”
- Avoid making every movement equally random.
- Avoid obvious repeated patterns.
- Avoid impossible precision when randomness is enabled.

---

## 9. Jitter, Tremor, and Micro-Corrections

Small noise can make movement feel alive, but too much makes it look broken.

### Types of small motion

- **Path drift**
  - Slow, smooth deviation from the ideal path.

- **Hand tremor**
  - Tiny high-frequency variation.
  - Should be subtle and usually reduced during precise final targeting.

- **Micro-corrections**
  - Small adjustments near the target.
  - More likely when the target is small or the movement was fast.

- **Settling motion**
  - Tiny movement after arriving near the target.
  - Useful before clicking.

- **Hesitation**
  - Very short slowdown or pause mid-movement.
  - Should be rare.

### Tuning guidance

- Use less jitter for dragging.
- Use less jitter while holding a precise target.
- Use more correction after overshoot.
- Use more correction for small targets.
- Use lower jitter at high speeds because rapid noise can look unnatural.
- Use input-device-style quantization so tiny noise does not produce impossible fractional movement.

---

## 10. Overshoot and Correction

Overshoot is one of the strongest realism tools when used sparingly.

### Overshoot features

- Chance-based overshoot.
- Speed-based overshoot.
- Distance-based overshoot.
- Target-size-based overshoot.
- Directional overshoot along the movement vector.
- Lateral overshoot offset.
- Undershoot for cautious movement.
- One or more correction movements.
- Slower corrective phase.
- Final settling before click.

### When overshoot should be more likely

- Long-distance movement.
- High speed.
- Small target.
- Low skill profile.
- Sudden target change.
- User is “fatigued” or under pressure.

### When overshoot should be less likely

- Large target.
- Slow movement.
- Repeated familiar action.
- Dragging.
- Accessibility mode with stabilization.
- Tutorial pointer movement where clarity matters more than realism.

---

## 11. Timing Model

Timing realism is just as important as path realism.

### Timing elements

- Reaction time before starting.
- Delay after target appears.
- Delay after previous action.
- Movement duration.
- Mid-path hesitation.
- Correction duration.
- Settling time before clicking.
- Button-down duration.
- Delay between double-clicks.
- Delay between drag start and movement.
- Delay after dropping an item.
- Scroll burst spacing.
- Pause between repeated actions.

### Timing distributions

- Use log-normal or Gaussian-like distributions for delays.
- Clamp minimum and maximum values.
- Make timing depend on task complexity.
- Make repeated actions become slightly faster.
- Add occasional longer pauses for decision-like behavior in NPC/tutorial systems.

---

## 12. Click Modeling

Clicks should have timing, position, and button-state realism.

### Click features

- Left, right, middle, and extra mouse buttons.
- Button down/up events.
- Variable button hold duration.
- Configurable click delay after movement.
- Double-click interval variation.
- Click position error within target bounds.
- Click only after cursor/camera has settled.
- Optional tiny movement during press.
- Cancellation if target becomes invalid.
- Different click behavior for UI, world objects, and camera interactions.

### Click quality rules

- Do not click exactly at the center every time.
- Prefer click points distributed inside the target area.
- For small targets, reduce endpoint error before clicking.
- For large targets, allow more variation.
- For drag actions, hold before moving far enough to exceed the drag threshold.

---

## 13. Dragging and Selection

Dragging behaves differently from normal cursor movement because the button is held and the user often maintains more control.

### Drag features

- Press delay before movement starts.
- Drag threshold handling.
- Smooth but stable path.
- Reduced jitter while holding.
- Slight initial tug after button down.
- Drop target correction.
- Variable release timing.
- Cancellation behavior.
- Auto-scroll near edges, if the UI supports it.
- Selection rectangle generation.
- Item pickup and release feedback timing.

### Drag use cases

- Inventory item movement.
- UI slider movement.
- Map panning.
- Text selection.
- File/card dragging.
- Strategy-game box selection.
- Editor gizmo manipulation.

---

## 14. Scroll Wheel and Trackpad Behavior

Scrolling should be modeled separately from pointer movement.

### Scroll features

- Wheel notch steps.
- Smooth scrolling.
- Scroll bursts.
- Variable spacing between wheel ticks.
- Acceleration for repeated scrolling.
- Deceleration near desired list position.
- Horizontal scrolling.
- Trackpad-style continuous scroll.
- Momentum scroll.
- Scroll cancellation.
- Overscroll and correction.

### Scroll tuning

- Menus usually need controlled scrolling.
- Long pages may use faster burst scrolling.
- Precision selection lists should slow near the target item.
- Trackpad and wheel profiles should feel different.

---

## 15. Camera and Relative Mouse Movement

Many games use mouse deltas for camera rotation instead of an absolute cursor position.

### Camera movement features

- Relative X/Y delta output.
- Yaw and pitch sensitivity.
- Separate horizontal and vertical sensitivity.
- Pitch limits.
- Y-axis inversion support.
- FOV-aware scaling.
- Zoom sensitivity scaling.
- Raw input conversion.
- Frame-rate-independent rotation.
- Smoothing options.
- Acceleration options.
- Maximum turn rate.
- Dead-zone handling if mixed with controller input.
- Recenter behavior for cinematic tools.

### Camera movement contexts

- First-person camera.
- Third-person orbit camera.
- Top-down camera pan.
- Strategy-game edge pan.
- Spectator camera.
- Photo mode.
- Cinematic rail camera.
- Editor viewport camera.

### Important distinction

Cursor movement targets a position. Camera movement usually targets an orientation or relative rotation. Treat these as separate systems that share timing/noise concepts but use different math.

---

## 16. Sensitivity, DPI, and Device Profiles

Device configuration affects how movement feels. Your engine should model or account for it explicitly.

### Device properties

- Mouse DPI.
- Polling rate.
- Sensor resolution.
- OS pointer speed.
- OS mouse acceleration.
- Raw input enabled/disabled.
- Game sensitivity.
- Scoped/zoom sensitivity.
- UI scale.
- Monitor resolution.
- Monitor refresh rate.
- Physical mousepad constraints, if simulating physical behavior.

### Player-style profiles

- Low-sensitivity player.
- High-sensitivity player.
- Smooth careful player.
- Fast flick-heavy player.
- New player.
- Experienced player.
- Fatigued player.
- Accessibility-stabilized profile.
- Cinematic/tutorial profile.

### Useful derived values

- Pixels per centimeter.
- Degrees per mouse unit.
- Centimeters per 360-degree camera turn.
- Movement time by target distance.
- Target difficulty index.
- Average endpoint error.

---

## 17. Target Acquisition

The engine needs to understand what it is moving toward.

### Target data to store

- Target center.
- Target bounds.
- Target shape.
- Clickable region.
- Target priority.
- Target movement velocity.
- Target visibility.
- Target lifetime.
- Required precision.
- Allowed miss radius.
- Whether target can move during the action.

### Target shapes

- Rectangle.
- Circle.
- Ellipse.
- Polygon.
- Text line.
- Slider track.
- Radial menu wedge.
- 3D projected bounding box.
- Screen-space hit area.

### Targeting behavior

- Pick a point inside the target, not always the center.
- Avoid edges unless intentional.
- For large targets, allow natural variation.
- For small targets, slow down and correct more.
- If target moves, replan smoothly instead of snapping.
- If target disappears, cancel or transition to fallback behavior.

---

## 18. Moving Targets

Moving targets require prediction and replanning.

### Moving-target features

- Target velocity estimation.
- Target acceleration estimation.
- Latency compensation.
- Prediction horizon.
- Confidence score.
- Smooth replanning.
- Correction if prediction fails.
- Abort if target becomes invalid.
- Speed adjustment for closing distance.

### Important rules

- Do not instantly teleport the planned path when the target moves.
- Blend from the current path into the new path.
- Prediction should be imperfect if realism matters.
- Increase correction behavior when the target changes direction.

---

## 19. Context Awareness

The same movement should not be used everywhere.

### Context-specific behavior

| Context | Recommended behavior |
|---|---|
| Main menu | Smooth, readable, low jitter. |
| Fast gameplay camera | Relative deltas, speed scaling, lower path curvature. |
| Inventory UI | Medium speed, moderate precision, click variation. |
| Dragging | Stable, reduced jitter, careful release. |
| Text selection | Slow, precise, low noise. |
| Map panning | Smooth drag, momentum optional. |
| Tutorial pointer | Clear and slightly idealized. |
| QA automation | Deterministic seed, high logging. |
| Accessibility assist | Stabilized movement, user-configurable. |
| Cinematic playback | Designer-controlled curves. |

---

## 20. State and Personality Profiles

For NPCs, tutorials, demos, and test agents, a profile system can make behavior consistent.

### Profile parameters

- Preferred speed.
- Preferred acceleration.
- Average reaction time.
- Reaction-time variance.
- Path curvature preference.
- Endpoint precision.
- Overshoot tendency.
- Correction style.
- Jitter level.
- Scroll style.
- Click timing style.
- Fatigue rate.
- Warm-up rate.
- Cautious versus aggressive movement.

### State modifiers

- Focused.
- Distracted.
- Rushed.
- Fatigued.
- Precise mode.
- Exploration mode.
- Tutorial mode.
- Accessibility mode.
- Cinematic mode.

---

## 21. Smoothing and Filtering

Smoothing improves motion quality, but too much can make input feel delayed.

### Smoothing options

- Exponential moving average.
- Critically damped spring smoothing.
- Low-pass filter.
- Kalman-style filtering for noisy targets.
- Spline interpolation.
- Velocity smoothing.
- Acceleration smoothing.
- Jerk limiting.

### Smoothing rules

- Use less smoothing for responsive gameplay camera input.
- Use more smoothing for tutorial/cinematic pointer motion.
- Avoid smoothing clicks or button events in a way that changes intent.
- Clamp extreme deltas.
- Ensure smoothing is frame-rate independent.

---

## 22. Quantization and Sampling

Real mouse input arrives in discrete samples, not perfect continuous motion.

### Sampling features

- Configurable update rate.
- Configurable device polling rate.
- Frame-rate-independent integration.
- Timestamped events.
- Sub-frame interpolation.
- Delta accumulation.
- Integer pixel or raw-unit quantization.
- Rounding error handling.
- Event batching.
- Dropped-frame recovery.

### Why this matters

Without sampling and quantization, movement can look mathematically perfect in a way that real input usually does not. With too much quantization, movement becomes choppy. The engine should support both polished visual motion and realistic input sampling, depending on the use case.

---

## 23. Error Modeling

A realistic engine should support imperfect outcomes, especially for small targets or fast movement.

### Error types

- Endpoint error.
- Directional error.
- Distance error.
- Click-location error.
- Timing error.
- Overshoot error.
- Undershoot error.
- Scroll-position error.
- Drag-release error.

### Error controls

- Error scale by target size.
- Error scale by movement speed.
- Error scale by profile skill.
- Error scale by fatigue.
- Error scale by input device.
- Error reduction during corrective phase.
- Maximum allowed error for critical UI actions.

---

## 24. Fatigue, Warm-Up, and Session Drift

For long simulations, movement should not be identical forever.

### Long-session features

- Slight speed changes over time.
- Slight reaction-time drift.
- Increased error when fatigued.
- Reduced error after warm-up.
- More hesitation after long inactivity.
- Different behavior after repeated mistakes.
- Short-term adaptation to repeated targets.
- Optional concentration/focus variable.

### Keep it subtle

These effects should be small unless the game intentionally exposes them. Heavy-handed fatigue can feel like broken input.

---

## 25. Accessibility Features

A mouse engine can also power helpful accessibility systems.

### Accessibility options

- Cursor stabilization.
- Tremor reduction.
- Slow mode.
- Precision mode.
- Sticky targets.
- Larger effective hit regions.
- Click confirmation.
- Dwell click.
- Adjustable double-click interval.
- Adjustable drag threshold.
- Reduced motion.
- Remappable mouse buttons.
- One-button alternatives.
- Smoothing presets.
- Per-axis sensitivity.

---

## 27. Recording, Replay, and Training Data

A high-quality engine benefits from real input samples, but data collection should be transparent and privacy-conscious.

### Recording features

- Timestamped mouse deltas.
- Cursor positions.
- Button states.
- Scroll events.
- Target information.
- Game context.
- Frame time.
- Sensitivity/DPI settings if the user consents.
- Movement outcome.
- Error and correction counts.

### Replay features

- Deterministic playback.
- Seeded random variation.
- Fixed-time playback.
- Frame-independent playback.
- Pause/resume.
- Speed scaling.
- Event inspection.
- Path visualization.

### Data ethics

- Collect samples only with consent.
- Avoid collecting unrelated desktop behavior.
- Anonymize data where possible.
- Let users opt out.
- Do not use recorded data to impersonate users.

---

---

## 29. Quality Metrics

Use metrics so tuning does not rely only on “looks good.”

### Movement quality metrics

| Metric | What it tells you |
|---|---|
| Path efficiency | Actual path length divided by direct distance. |
| Peak speed | Whether movement has unrealistic spikes. |
| Time to target | Whether movement duration fits the task. |
| Endpoint error | Final accuracy. |
| Overshoot rate | How often the motion passes the target. |
| Correction rate | How often final adjustments occur. |
| Jerk | How smooth or twitchy the path is. |
| Click timing variance | Whether clicks are too identical. |
| Directional bias | Whether curves always bend the same way. |
| Repeatability | Whether seeded runs are stable. |
| Frame independence | Whether behavior changes with FPS. |
| Latency impact | Whether smoothing adds too much delay. |

---
---

## 33. Common Mistakes to Avoid

- Moving in perfectly straight lines every time.
- Using the same speed for every distance.
- Clicking exactly at the target center every time.
- Adding pure random jitter to every point.
- Making randomness independent every frame.
- Ignoring target size.
- Ignoring frame-rate independence.
- Ignoring DPI and sensitivity.
- Ignoring raw input versus OS cursor input.
- Forgetting button-down duration.
- Forgetting scroll behavior.
- Making overshoot too frequent.
- Making movement too smooth and mathematically perfect.
- Making movement too noisy and unstable.
- Replanning instantly when targets move.
- Failing to log seeds and parameters.

---


## 35. Suggested Data Structures

### `MouseTarget`

Stores target information:

- ID.
- Type.
- Center position.
- Bounds.
- Shape.
- Clickable region.
- Movement velocity.
- Required precision.
- Priority.
- Validity state.

### `MouseProfile`

Stores behavior style:

- Speed preference.
- Acceleration preference.
- Reaction-time range.
- Jitter level.
- Curve preference.
- Overshoot tendency.
- Correction tendency.
- Click timing.
- Scroll style.
- Accessibility modifiers.

### `MouseMovementPlan`

Stores one planned action:

- Start position.
- Target position.
- Target bounds.
- Duration.
- Curve type.
- Control points.
- Speed profile.
- Noise settings.
- Overshoot plan.
- Correction plan.
- Click/drag/scroll action.
- Random seed.

### `MouseEvent`

Stores emitted events:

- Timestamp.
- Position or delta.
- Button state.
- Scroll delta.
- Coordinate space.
- Source mode.
- Debug tags.

---

## 36. Best Default Behavior

A strong default movement pipeline could work like this:

1. Receive a target and context.
2. Convert target into the correct coordinate space.
3. Pick a realistic target point inside the clickable region.
4. Estimate movement difficulty from distance and target size.
5. Choose duration using a Fitts-style model.
6. Generate a minimum-jerk timing curve.
7. Generate a slightly curved path.
8. Add low-frequency path drift.
9. Add subtle context-aware micro-jitter.
10. Optionally undershoot or overshoot.
11. Add one or more corrective movements.
12. Settle near the target.
13. Execute click, drag, scroll, or camera action.
14. Record metrics and debug data.

---

## 37. Practical Tuning Advice

Start with clean, smooth movement before adding randomness. Then add one realism layer at a time and measure the result.

Recommended tuning order:

1. Coordinate conversion correctness.
2. Basic motion duration.
3. Smooth speed profile.
4. Curved path generation.
5. Endpoint variation.
6. Click timing variation.
7. Corrective movements.
8. Overshoot behavior.
9. Jitter and drift.
10. Profiles and session state.

The best engine is not the one with the most randomness. It is the one where speed, path, error, timing, and corrections all match the task being performed.
