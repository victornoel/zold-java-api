/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Yegor Bugayenko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.zold.api;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Iterator;
import org.cactoos.Scalar;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.io.Directory;
import org.cactoos.iterable.Filtered;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.StickyScalar;
import org.cactoos.scalar.SyncScalar;

/**
 * Wallets in path.
 *
 * @author Tolegen Izbassar (t.izbassar@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCoupling (2 lines)
 */
public final class WalletsIn implements Wallets {

    /**
     * Path containing wallets.
     */
    private final IoCheckedScalar<Path> path;

    /**
     * Filter for matching file extensions.
     */
    private final IoCheckedFunc<Path, Boolean> filter;

    /**
     * Ctor.
     * @param pth Path with wallets
     */
    public WalletsIn(final Path pth) {
        this(
            () -> pth,
            "z"
        );
    }

    /**
     * Ctor.
     * @param pth Path with wallets
     * @param ext File extension to match
     */
    public WalletsIn(final Scalar<Path> pth, final String ext) {
        this.path = new IoCheckedScalar<>(
            new SyncScalar<>(
                new StickyScalar<>(pth)
            )
        );
        this.filter = new IoCheckedFunc<Path, Boolean>(
            (file) -> file.toFile().isFile()
                && FileSystems.getDefault()
                    .getPathMatcher(String.format("glob:**.%s", ext))
                    .matches(file)
        );
    }

    // @todo #4:30min Return the new instance of the Wallet, that will
    //  be created in the path with all wallets. Should be taken care of
    //  after Wallet interface will have implementations. Cover with tests and
    //  remove irrelevant test case.
    @Override
    public Wallet create() {
        throw new UnsupportedOperationException("create() not yet supported");
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Iterator<Wallet> iterator() {
        try {
            return new Mapped<Path, Wallet>(
                (pth) -> new Wallet.File(pth),
                new Filtered<>(this.filter, new Directory(this.path.value()))
            ).iterator();
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
