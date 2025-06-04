import math as _math
import random as rnd
import time
import builtins

pin_modes = {}
digital_pins = {}
analog_pins = {}

def setup():
    pass

def loop():
    pass

def serialprintln(text):
    builtins.print(str(text))

def serialprint(text):
    builtins.print(str(text), end="")

def serialbegin(baud):
    serialprintln(f"Serial communication started at {baud} baud.")

def delay(milliseconds):
    time.sleep(milliseconds / 1000.0)

def delaymicroseconds(microseconds):
    time.sleep(microseconds / 1_000_000.0)

def pinmode(pin, mode):
    pin_modes[pin] = mode
    digital_pins[pin] = 0
    analog_pins[pin] = 0

def digitalwrite(pin, voltage):
    if pin in pin_modes and pin_modes[pin] == "OUTPUT":
        digital_pins[pin] = 1 if voltage == "HIGH" else 0
    else:
        serialprintln(f"Error: Pin {pin} not set as OUTPUT")

def analogwrite(pin, pwm):
    if pin in pin_modes and pin_modes[pin] == "OUTPUT":
        analog_pins[pin] = pwm
    else:
        serialprintln(f"Error: Pin {pin} not set as OUTPUT")

def digitalread(pin):
    if pin in pin_modes and pin_modes[pin] == "INPUT":
        return digital_pins.get(pin, 0)
    else:
        serialprintln(f"Warning: Reading from pin {pin} not set as INPUT")
        return 0

def analogread(pin):
    if pin in pin_modes and pin_modes[pin] == "INPUT":
        return analog_pins.get(pin, 0)
    else:
        serialprintln(f"Warning: Reading from pin {pin} not set as INPUT")
        return 0

def abs(x):
    return builtins.abs(x)

def constrain(value, min_val, max_val):
    return max(min_val, min(value, max_val))

def map(value, fromlow, fromhigh, tolow, tohigh):
    if fromhigh - fromlow == 0:
        raise ValueError("Invalid input range")
    return (value - fromlow) * (tohigh - tolow) / (fromhigh - fromlow) + tolow

def max(a, b):
    return builtins.max(a, b)

def min(a, b):
    return builtins.min(a, b)

def pow(base, exponent):
    return _math.pow(base, exponent)

def sqrt(value):
    return _math.sqrt(value)

def sq(value):
    return value * value

def randomseed(seed):
    rnd.seed(seed)

def random(min_val, max_val):
    return rnd.randrange(min_val, max_val)

def run():
    pass
    #at the end of the script
