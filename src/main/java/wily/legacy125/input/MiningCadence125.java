package wily.legacy125.input;

/** Runs survival block damage at Minecraft's 20 Hz tick rate while preserving instant mining. */
public final class MiningCadence125 {
    private final RepeatCooldown125 survivalTicks = new RepeatCooldown125(50L);

    public boolean shouldDamage(boolean attacking, boolean instantMine, long nowMillis) {
        return attacking && (instantMine || survivalTicks.shouldFire(true, nowMillis));
    }
}
