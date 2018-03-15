I finished my GrowWithGoogle / Udacity Android Development class in less than a month,
so I began fishing around for other projects to try out. A few months ago, I had
implemented a full-featured, BlackJack game in Unity / C#. It was a UI-driven game and
I felt comfortable that I had learned enough XML and Java to port the game to Android.

The original Unity / C# code is here https://github.com/sdocy/blackjack, project
website https://sdocy.github.io/blackjack/

Highlights:

- Most of code actually ported quite easily.

- Even swapping the Unity button, text and image objects for XML button, text and
  image views was pretty straightforward.
  
- Getting proper game delays was trickier than in Unity, but it gave me a chance to
  play with Java multi-threading.
  

- I did not port support for splitting cards. Being able to display multiple player
  hands on a small mobile screen will require a new card layout approach.

- The absence of pass-by-reference in Java resulted in my creating a player class to
be able to have Java methods that work on both the player's hand and the dealer's hand.
This actually turned out extremely well. Not only could I re-use the code I wanted for
both the player and the dealer, but it made the blackjack code feel much more
streamlined. Go go OOD!