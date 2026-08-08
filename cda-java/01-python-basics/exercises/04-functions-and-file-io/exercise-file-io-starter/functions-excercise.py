def red_light():
    print("Stop!The light is red.")


def yellow_light():
    print("Caution!  The light is yellow.")


def green_light():
    print("Go!  The light is green.")



def traffic_light(state):
    if state == "red":
        red_light()
    elif state == "yellow":
        yellow_light()
    elif state == "green":
        green_light()
    else:
        print("Error: Invalid traffic light state.")


traffic_light("red")
traffic_light("yellow")
traffic_light("green")
traffic_light("blue")
