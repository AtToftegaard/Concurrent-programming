/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cp;

import java.nio.file.Path;

/**
 *
 * @author Andreas
 */
public class ResultObject implements Result {

    public Path path;
    public int line;

    @Override
    public Path path() {
        return path;
    }

    @Override
    public int line() {
        return line;
    }
    
}
