# Words Stacks

## Game Intro:

The idea of the game is to try to separate out two words of the same length whose letters have been scrambled 
(but the order of the letters has been preserved).
For example, 'cat' and 'dog' might get scrambled in many ways but 'c' will always come before 'a' and 'a' will always come before 't'. 
Similarly, 'd' will always come before 'o' and 'o' will always come before 'g'. So we get permutations like:
  * cdaotg
  * cadogt
  * catdog
  * dogcat
  * dcoagt
  * ...
... But never 'actdog' ('a' is out of order) or 'coatdg' ('o' out of order).


## Data Structures used in code:
* Stack

## Additions:
The code follows the CS Android tutorial with a few modications including but not limited to:
* changing the colour the text and display boxes based on whether the user guessed right or not
* disabling the undo button at the appropriate times to avoid game crash
* preventing the user putting more x characters in any one box, where x is the length of the words to be guessed
