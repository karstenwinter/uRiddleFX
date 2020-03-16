package uriddle.logic;

public class Switch {
  public enum SwitchType {
    ALWAYS, TOGGLE
  }

  public SwitchType type;

  public Switch(SwitchType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "Switch " + type;
  }
}
