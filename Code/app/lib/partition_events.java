import java.io.File;
import java.util.*;
public class partition_events {
  public static void main(String[] args) {
    int EV_SYN = 0;
    int EV_KEY = 1;
    int EV_ABS = 3;
    int ABS_X = 0;
    int ABS_Y = 1;
    int ABS_MT_POSITION_X = 53;
    int ABS_MT_POSITION_Y = 54;
    int ABS_MT_TRACKING_ID = 57;
    int SYN_REPORT = 0;
    int BTN_TOUCH = 330;

    float LONG_CLICK_DURATION = 0.5;
    int CLICK_RING = 20;
    int x = 0;
    int y = 0;
    boolean was_finger_down = False
    boolean finger_down = False
    ArrayList events = new ArrayList();
    ArrayList coords = new ArrayList();
    File input_file = new File(args[1]);
    File sdcard = new File(sdCard.getAbsolutePath() + "/filename");;
  }

}
pattern = re.compile(
        r'\[\s*(\d*\.\d*)\] /dev/input/event(\d): ' \
        r'([0-9a-f]{4}) ([0-9a-f]{4}) ([0-9a-f]{8})')

for line in input_file:
    if line[0] != '[':
        continue
    time, device, type, code, value = re.match(pattern, line).groups()
    time = float(time)
    device = int(device)
    type = int(type, 16)
    code = int(code, 16)
    value = int(value, 16)
    event = device, type, code, value
    events.append(event)
    
    # Button down.
    if type == EV_KEY:

        # If the touch screen has been toggled, let sync events
        # handle the logic.
        if code == BTN_TOUCH:
