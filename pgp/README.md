# Import PGP keys

Execute `gpg --import` commands against the two asc files and then check for the new keys with `gpg -k` and `gpg -K`.

```bash
gpg --import private.pgp
gpg --import public.pgp
gpg -K
gpg -k
```