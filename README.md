# Android Robotics
 

## Loop through bitmap and find red pixels

## Search for ball candidates
In order to not track every red pixel as an potential ball, a choice was made to simplify this process.
In order to make a candidate for being a ball, a group of pixel of the size of 40 was created.
## determine if red pixel group is a ball
In order to determine if a group of red pixels is a ball, the following algorithm was used

1. Measure the maximal width of the group of red pixels.
2. Measure the maximal height of the group of red pixels
3. compare height and width and check if they are in the same range.
4. If both measurements are in the same range, determine the pixels as ball and mark the group.
   If both measurement are not in the same range, determine the pixels not as ball and skip the group.


## Controls via size and position
If the Ball is smaller than the optimum, the ball is too far away so the robot must move forward.
If the Ball is bigger than the optimum, the ball is to close so the robot must move backward.
If the Ball changes position to the right, the ball moved to the right so the robot has to turn as well.
If the Ball changes position to the left, the ball moved to the left so the robot has to turn as well.