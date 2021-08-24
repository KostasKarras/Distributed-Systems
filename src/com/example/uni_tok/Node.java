package com.example.uni_tok;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

interface Node {

    public void initialize(int port) throws UnknownHostException;

    public void connect();

    public void disconnect();
}
