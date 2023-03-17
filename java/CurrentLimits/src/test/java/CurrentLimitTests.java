import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ctre.phoenixpro.configs.CurrentLimitsConfigs;
import com.ctre.phoenixpro.configs.TalonFXConfiguration;
import com.ctre.phoenixpro.controls.DutyCycleOut;
import com.ctre.phoenixpro.hardware.TalonFX;
import com.ctre.phoenixpro.unmanaged.Unmanaged;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.Timer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CurrentLimitTests {
    final double SET_DELTA = 0.1;

    TalonFX talon;

    @BeforeEach
    public void constructDevices() {
        assert HAL.initialize(500, 0);

        talon = new TalonFX(0);
    }

    @Test
    public void testStatorLimit() {
        final double TALON_POSITION = 0.4;

        var statorCurrent = talon.getStatorCurrent();
        
        /* Configure a stator limit of 20 amps */
        TalonFXConfiguration toConfigure = new TalonFXConfiguration();
        CurrentLimitsConfigs currentLimitConfigs = new CurrentLimitsConfigs();
        currentLimitConfigs.StatorCurrentLimit = 20;
        currentLimitConfigs.StatorCurrentLimitEnable = false; // Start with stator limits off
        toConfigure.CurrentLimits = currentLimitConfigs;

        talon.getConfigurator().apply(toConfigure);

        var simState = talon.getSimState();

        /* Put the talon in a stall, which should produce a lot of current */
        talon.setControl(new DutyCycleOut(1));
        waitWhileEnabled(0.5);

        /* Get the next update for stator current */
        statorCurrent.waitForUpdate(1);

        System.out.println("Stator current is " + statorCurrent);
        assertTrue(statorCurrent.getValue() > 100); // Stator current should be in excess of 100 amps

        /* Now apply the stator current limit */
        currentLimitConfigs.StatorCurrentLimitEnable = true;
        talon.getConfigurator().apply(currentLimitConfigs);

        waitWhileEnabled(0.5);

        /* Get the next update for stator current */
        statorCurrent.waitForUpdate(1);
        
        System.out.println("Stator current is " + statorCurrent);
        assertTrue(statorCurrent.getValue() < 25); // Give some wiggle room
    }

    @Test
    public void testSupplyLimit() {
        final double TALON_POSITION = 0.4;

        var supplyCurrent = talon.getSupplyCurrent();
        
        /* Configure a supply limit of 20 amps */
        TalonFXConfiguration toConfigure = new TalonFXConfiguration();
        CurrentLimitsConfigs currentLimitConfigs = new CurrentLimitsConfigs();
        currentLimitConfigs.SupplyCurrentLimit = 5;
        currentLimitConfigs.SupplyCurrentThreshold = 10;
        currentLimitConfigs.SupplyTimeThreshold = 1.0;
        currentLimitConfigs.StatorCurrentLimitEnable = false; // Start with supply limits off
        toConfigure.CurrentLimits = currentLimitConfigs;

        talon.getConfigurator().apply(toConfigure);

        var simState = talon.getSimState();

        /* Put the talon in a stall, which should produce a lot of current */
        talon.setControl(new DutyCycleOut(1));
        waitWhileEnabled(3);

        /* Get the next update for supply current */
        supplyCurrent.waitForUpdate(1);

        System.out.println("Supply current is " + supplyCurrent);
        assertTrue(supplyCurrent.getValue() > 100); // Supply current should be in excess of 100 amps

        /* Now apply the supply current limit */
        currentLimitConfigs.SupplyCurrentLimitEnable = true;
        talon.getConfigurator().apply(currentLimitConfigs);

        waitWhileEnabled(0.5);

        /* Get the next update for supply current */
        supplyCurrent.waitForUpdate(1);
        
        System.out.println("Supply current is " + supplyCurrent);
        assertTrue(supplyCurrent.getValue() > 100); // Make sure it's still over 100 amps (time hasn't exceeded 1 second in total)
        
        /* Wait a full extra couple seconds so the limit kicks in and starts limiting us */
        waitWhileEnabled(2);

        /* Get the next update for supply current */
        supplyCurrent.waitForUpdate(1);
        
        System.out.println("Supply current is " + supplyCurrent);
        assertTrue(supplyCurrent.getValue() < 10); // Give some wiggle room
    }

    private void waitWhileEnabled(double timeToWait){
        /* In 100ms increments, feed enable and wait 100ms */
        for(double i = 0; i < timeToWait; i += 0.1) {
            Unmanaged.feedEnable(200); // Feed enable for 200ms
            Timer.delay(0.1); // Wait for 100ms
        }
    }
}