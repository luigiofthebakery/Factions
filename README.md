Compile using Maven, in the Factions folder:
`mvn clean install`

Then merge the generated file `target/Factions.jar` with the Factions-1.0.0.jar original file (this is linked during the compilation for some needed dependencies).

Copy over the `com` folder and `plugin.yml` from the newly generated jar into a copy of the original Factions-1.0.0.jar.
