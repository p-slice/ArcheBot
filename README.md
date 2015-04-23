ArcheBot
========

It has occurred to me I should update this file more often - a year after creating it...

ArcheBot is an IRC bot platform written in Java. It is designed to give programmers a variety of tools to simplify the task of communicating with the IRC server. As well as keeping track of information such as what users are in what channel, it includes features not always found in other similar platforms, such as a fully integrated command system, built-in property saving/loading, and more.

The abstract Command class allows you to write code that runs when the execute method is called, typically when a command event occurs after a user sends a message starting with a set prefix. The bot keeps track of what permissions users have, and can be set to automatically send error messages if a user doesn't have the necessary permission. Pre-loaded permissions include permission.operator, permission.default (assigned to all users) and permission.ignore.

To save and load properties, ArcheBot uses a markup language I wrote called PML. It's simple, flexible, and works well storing a large variety of properties, permissions, and other user generated data.

Although there are a great number of other features, they're often changing as I upgrade the platform with new features. I'm happy to answer any questions about current capabilities and how to use them.
