// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

#include "Robot.h"
#include <iostream>

using namespace ctre::phoenix6;

void Robot::RobotInit() {
  /* Configure the Talon to use a supply limit of 60 amps IF we exceed 80 amps for over 0.1 seconds */
  configs::TalonFXConfiguration toConfigure{};
  m_currentLimits.SupplyCurrentLimit = 60; // Limit to 60 amps
  m_currentLimits.SupplyCurrentThreshold = 80; // If we exceed 80 amps
  m_currentLimits.SupplyTimeThreshold = 0.1; // For at least 0.1 second
  m_currentLimits.SupplyCurrentLimitEnable = true; // And enable it

  m_currentLimits.StatorCurrentLimit = 120; // Limit stator to 120 amps
  m_currentLimits.StatorCurrentLimitEnable = true; // And enable it

  toConfigure.CurrentLimits = m_currentLimits;

  m_fx.GetConfigurator().Apply(toConfigure);
}

void Robot::RobotPeriodic() {
  /* Tie output of joystick to output of motor for current limit testing */
  m_fx.SetControl(m_output.WithOutput(m_joystick.GetLeftY()));

  if (m_joystick.GetAButtonPressed()) {
    /* Toggle the supply limit enable */
    m_currentLimits.SupplyCurrentLimitEnable ^= true;
    std::cout << "Setting supply limit to " << m_currentLimits.SupplyCurrentLimitEnable << std::endl;
    m_fx.GetConfigurator().Apply(m_currentLimits);
  }
  if (m_joystick.GetBButtonPressed()) {
    /* Toggle the stator limit enable */
    m_currentLimits.StatorCurrentLimitEnable ^= true;
    std::cout << "Setting stator limit to " << m_currentLimits.StatorCurrentLimitEnable << std::endl;
    m_fx.GetConfigurator().Apply(m_currentLimits);
  }
  if (printCount++ > 20) {
    printCount= 0;
    std::cout << "Supply current: " << m_fx.GetSupplyCurrent() << std::endl;
    std::cout << "Stator current: " << m_fx.GetStatorCurrent() << std::endl;
  }
}

void Robot::AutonomousInit() {}
void Robot::AutonomousPeriodic() {}

void Robot::TeleopInit() {}
void Robot::TeleopPeriodic() {}

void Robot::DisabledInit() {}
void Robot::DisabledPeriodic() {}

void Robot::TestInit() {}
void Robot::TestPeriodic() {}

void Robot::SimulationInit() {}
void Robot::SimulationPeriodic() {}

#ifndef RUNNING_FRC_TESTS
int main() {
  return frc::StartRobot<Robot>();
}
#endif
