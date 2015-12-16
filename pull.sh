#!/bin/sh
sudo service eventkicker stop

git pull origin master

# Create this if don't exist
sudo mkdir /opt/eventkicker
sudo mkdir /etc/startdown
sudo mkdir /var/log/eventkicker

sudo touch /etc/startdown/eventkicker.conf
sudo touch /etc/startdown/eventkicker_logback.xml

sudo cp eventkicker /etc/init.d/eventkicker
sudo chmod +x /etc/init.d/eventkicker
sudo service eventkicker start

# sudo update-rc.d eventkicker defaults
